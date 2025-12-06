pipeline {
	agent any
	tools {
		maven 'Maven3.9.9'
		jdk 'jdk-25'
	}
	environment {
		DOCKER_REGISTRY = '192.168.0.82:5000'
		APP_NAME = 'zipp'
		DOCKER_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}:${GIT_COMMIT_SHORT}"
		DOCKER_IMAGE_LATEST = "${DOCKER_REGISTRY}/${APP_NAME}:latest"
		GIT_REPO_URL = 'https://github.com/fungover/zipp'
		GIT_BRANCH = 'main'
		SSH_CREDENTIALS_ID = 'jenkins-ssh'
		CONTROL_PLANE_IP = '192.168.0.142'
		SSH_USER = 'root'
		K8S_MANIFEST_PATH = '/root/kubernetes/manifests/applications'
		K8S_MANIFEST_DIR = 'zipp'
		K8S_NAMESPACE = 'default'
		APP_PORT = '8080'
		SERVICE_PORT = '80'
		REPLICAS = '3'
		CPU_REQUEST = '100m'
		MEMORY_REQUEST = '256Mi'
		CPU_LIMIT = '500m'
		MEMORY_LIMIT = '512Mi'
		MYSQL_SECRET_NAME = 'mysql-auth'
		MYSQL_SECRET_USERNAME_KEY = 'username'
		MYSQL_SECRET_PASSWORD_KEY = 'password'
		MYSQL_URL_SECRET_NAME = 'mysql-url'
		MYSQL_URL_SECRET_KEY = 'url'
		SPRING_MYSQL_DATABASE = 'appdb'
		DOMAIN = 'zipp.city'
		CLUSTER_ISSUER = 'letsencrypt-prod'
		KAFKA_BOOTSTRAP_SERVERS = 'my-kafka-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092'
		IS_CACHED = ''
	}

	stages {
		stage('Init') {
			steps {
				script {
					env.PROGRESS_CHECK_NAME = env.BRANCH_NAME == 'main' ? 'Deployment' : 'PR Build'
					env.PROGRESS_CHECK_TITLE = env.BRANCH_NAME == 'main' ? 'Starting deployment' : 'Starting PR Build'
					env.PROGRESS_CHECK_SUMMARY = env.BRANCH_NAME == 'main' ? 'Deployment pipeline initiated' : 'PR Build pipeline initiated'
				}
			}
		}

		stage('Checkout') {
			steps {
				checkout([
					$class: 'GitSCM',
					branches: [[name: "*/${env.BRANCH_NAME}"]],
					extensions: [[$class: 'CloneOption', shallow: false, depth: 0]],
					userRemoteConfigs: [[url: env.GIT_REPO_URL]]
				])
				script {
					def fullCommit = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
					env.GIT_COMMIT_SHORT = fullCommit.take(7) 
					env.DOCKER_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}:${GIT_COMMIT_SHORT}"
					env.DOCKER_IMAGE_LATEST = "${DOCKER_REGISTRY}/${APP_NAME}:latest"
					echo "DEBUG: GIT_COMMIT=${fullCommit}, GIT_COMMIT_SHORT=${GIT_COMMIT_SHORT}, DOCKER_IMAGE=${DOCKER_IMAGE}"
				}
				publishChecks name: PROGRESS_CHECK_NAME, title: PROGRESS_CHECK_TITLE, status: 'QUEUED', summary: PROGRESS_CHECK_SUMMARY
			}
		}
		stage('Verify Dockerfile') {
			steps {
				script {
					if (!fileExists('backend/Dockerfile')) error 'Dockerfile not found'
				}
			}
		}
		stage('Try Reuse PR Image') {
			when {
				branch 'main'
			}
			steps {
				script {
					def parentsOutput = sh(script: 'git log -1 --pretty=format:%P', returnStdout: true).trim()
					def parents = parentsOutput.split(/\s+/)
					if (parents.size() > 1) {
						def prCommit = parents[1]
						try {
							sh "git rev-parse ${prCommit} >/dev/null"
							def prShort = sh(script: "git rev-parse --short ${prCommit}", returnStdout: true).trim()
							def cachedImage = "${DOCKER_REGISTRY}/${APP_NAME}:${prShort}"
							try {
								sh "docker pull ${cachedImage}"
								env.DOCKER_IMAGE = cachedImage
								env.IS_CACHED = 'true'
								echo "Reusing cached image from PR: ${cachedImage}"
								publishChecks name: PROGRESS_CHECK_NAME, title: 'Reusing PR image', status: 'SUCCESS', summary: 'Pulled cached Docker image from approved PR'
							} catch (Exception e) {
								env.IS_CACHED = 'false'
								echo "No cached image found (or pull failed), will build fresh."
							}
						} catch (Exception e) {
							env.IS_CACHED = 'false'
							echo "WARNING: Invalid PR commit detected—building fresh image."
						}
					} else {
						env.IS_CACHED = 'false'
						echo "WARNING: Not a merge commit—skipping image reuse and building fresh."
					}
				}
			}
		}
		stage('Build Docker Image') {
			when {
				not {
					allOf {
						branch 'main'
						expression { env.IS_CACHED == 'true' }
					}
				}
			}
			steps {
				publishChecks name: PROGRESS_CHECK_NAME, title: 'Building image', status: 'IN_PROGRESS', summary: 'Docker build in progress'
				echo 'Building Docker image...'
				sh 'mvn -f backend/pom.xml clean package -DskipTests'
				sh 'docker build -t ${DOCKER_IMAGE} --no-cache -f backend/Dockerfile backend/.'
			}
		}
		stage('Scan Image for Vulnerabilities') {
			when {
				not {
					allOf {
						branch 'main'
						expression { env.IS_CACHED == 'true' }
					}
				}
			}
			steps {
				publishChecks name: PROGRESS_CHECK_NAME, title: 'Scanning image', status: 'IN_PROGRESS', summary: 'Vulnerability scan in progress'
				sh '''
            export PATH="$HOME/bin:$PATH"
            trivy clean --scan-cache
            trivy image --exit-code 1 --no-progress --severity HIGH,CRITICAL ${DOCKER_IMAGE}
        '''
			}
		}
		stage('Push Docker Image') {
			steps {
				publishChecks name: PROGRESS_CHECK_NAME, title: 'Pushing image', status: 'IN_PROGRESS', summary: 'Docker push in progress'
				sh '''
                    docker push ${DOCKER_IMAGE}
                '''
				script {
					if (env.BRANCH_NAME == 'main') {
						sh '''
                            docker tag ${DOCKER_IMAGE} ${DOCKER_IMAGE_LATEST}
                            docker push ${DOCKER_IMAGE_LATEST}
                        '''
					}
				}
			}
		}
		stage('PR Deployability Check') {
			when {
				not { branch 'main' }
			}
			steps {
				echo "PR build successful! Docker image '${DOCKER_IMAGE}' has been built, scanned, and pushed to the registry. This PR is deployable—once merged to main, the image will be tagged as 'latest' and deployed to Kubernetes."
				publishChecks name: 'PR Readiness', title: 'Ready for Merge', status: 'COMPLETED', conclusion: 'SUCCESS', summary: "Image '${DOCKER_IMAGE}' ready. Merge to deploy."
			}
		}
		stage('Generate Kubernetes Manifests') {
			when {
				branch 'main'
			}
			steps {
				publishChecks name: PROGRESS_CHECK_NAME, title: 'Generating manifests', status: 'IN_PROGRESS', summary: 'k8s manifest generation in progress'
				script {
					sh "mkdir -p ${K8S_MANIFEST_DIR}/{deployments,services,hpas,ingresses}"
					writeFile file: "${K8S_MANIFEST_DIR}/deployments/deployment.yaml", text: """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${APP_NAME}
  namespace: ${K8S_NAMESPACE}
spec:
  replicas: ${REPLICAS}
  selector:
    matchLabels:
      app: ${APP_NAME}
  template:
    metadata:
      labels:
        app: ${APP_NAME}
    spec:
      containers:
      - name: ${APP_NAME}
        image: ${DOCKER_IMAGE}
        ports:
        - containerPort: ${APP_PORT}
        resources:
          requests:
            cpu: "${CPU_REQUEST}"
            memory: "${MEMORY_REQUEST}"
          limits:
            cpu: "${CPU_LIMIT}"
            memory: "${MEMORY_LIMIT}"
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: ${MYSQL_URL_SECRET_NAME}
              key: ${MYSQL_URL_SECRET_KEY}
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: ${MYSQL_SECRET_NAME}
              key: ${MYSQL_SECRET_USERNAME_KEY}
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ${MYSQL_SECRET_NAME}
              key: ${MYSQL_SECRET_PASSWORD_KEY}
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "${KAFKA_BOOTSTRAP_SERVERS}"
        - name: GOOGLE_CLIENT_ID
          valueFrom:
            secretKeyRef:
              name: google-oauth2-credentials
              key: GOOGLE_CLIENT_ID
        - name: GOOGLE_CLIENT_SECRET
          valueFrom:
            secretKeyRef:
              name: google-oauth2-credentials
              key: GOOGLE_CLIENT_SECRET
"""
					writeFile file: "${K8S_MANIFEST_DIR}/services/service.yaml", text: """
apiVersion: v1
kind: Service
metadata:
  name: ${APP_NAME}-service
  namespace: ${K8S_NAMESPACE}
spec:
  selector:
    app: ${APP_NAME}
  type: ClusterIP
  ports:
  - port: ${SERVICE_PORT}
    targetPort: ${APP_PORT}
"""
					writeFile file: "${K8S_MANIFEST_DIR}/hpas/hpa.yaml", text: """
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ${APP_NAME}-hpa
  namespace: ${K8S_NAMESPACE}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ${APP_NAME}
  minReplicas: 3
  maxReplicas: 6
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
"""
					writeFile file: "${K8S_MANIFEST_DIR}/ingresses/ingress.yaml", text: """
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${APP_NAME}-ingress
  namespace: ${K8S_NAMESPACE}
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
    nginx.ingress.kubernetes.io/affinity: cookie
    nginx.ingress.kubernetes.io/session-cookie-name: INGRESSCOOKIE
    nginx.ingress.kubernetes.io/session-cookie-expires: "172800"
    nginx.ingress.kubernetes.io/session-cookie-max-age: "172800"
    nginx.ingress.kubernetes.io/affinity-mode: balanced
    cert-manager.io/cluster-issuer: "${CLUSTER_ISSUER}"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
      - ${DOMAIN}
    secretName: ${APP_NAME}-tls
  rules:
  - host: ${DOMAIN}
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ${APP_NAME}-service
            port:
              number: ${SERVICE_PORT}
"""
				}
			}
		}
		stage('Deploy to Kubernetes via SSH') {
			when {
				branch 'main'
			}
			steps {
				publishChecks name: PROGRESS_CHECK_NAME, title: 'Deploying to k8s', status: 'IN_PROGRESS', summary: 'k8s deployment in progress'
				withCredentials([sshUserPrivateKey(credentialsId: SSH_CREDENTIALS_ID, keyFileVariable: 'SSH_KEY')]) {
					sh '''
                        set -e
                        set -o pipefail
                        ssh -i ${SSH_KEY} -o StrictHostKeyChecking=accept-new -o UserKnownHostsFile=~/.ssh/known_hosts ${SSH_USER}@${CONTROL_PLANE_IP} "
                            mkdir -p ${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/{deployments,services,hpas,ingresses}
                        "
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/deployments/deployment.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/deployments/ || { echo "SCP deployment.yaml failed"; exit 1; }
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/services/service.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/services/ || { echo "SCP service.yaml failed"; exit 1; }
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/hpas/hpa.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/hpas/ || { echo "SCP hpa.yaml failed"; exit 1; }
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/ingresses/ingress.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/ingresses/ || { echo "SCP ingress.yaml failed"; exit 1; }
                        ssh -i ${SSH_KEY} -o StrictHostKeyChecking=accept-new -o UserKnownHostsFile=~/.ssh/known_hosts ${SSH_USER}@${CONTROL_PLANE_IP} "
                            set -e
                            kubectl apply -f ${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/services/
                            kubectl apply -f ${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/deployments/
                            kubectl apply -f ${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/hpas/
                            kubectl apply -f ${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/ingresses/
                            kubectl rollout status deployment/${APP_NAME} -n ${K8S_NAMESPACE}
                        "
                    '''
				}
			}
		}
	}
	post {
		always {
			script {
				def checkName = env.BRANCH_NAME == 'main' ? 'Deployment' : 'PR Build'
				def summary = env.BRANCH_NAME == 'main' ? "Deployment ${currentBuild.currentResult}" : "PR build and scan successful. Image ready for deployment on merge."
				try {
					publishChecks name: checkName, title: "${checkName} complete", status: 'COMPLETED', conclusion: currentBuild.resultIsBetterOrEqualTo('SUCCESS') ? 'SUCCESS' : 'FAILURE', summary: summary, detailsURL: env.BUILD_URL
				} catch (Exception e) {
					echo "Failed to publish checks: ${e.message}"
				}

				if (env.CHANGE_ID) {
					try {
						for (comment in pullRequest.comments) {
							if (comment.user == 'Jenkins-CD-for-Zipp') {
								pullRequest.deleteComment(comment.id)
							}
						}

						def buildStatus = currentBuild.currentResult
						def buildDuration = "${currentBuild.durationString.replace(' and counting', '')}"
						def message = """
**Jenkins Build #${env.BUILD_ID} Summary** (for PR #${env.CHANGE_ID})
- **Status**: ${buildStatus}
- **Duration**: ${buildDuration}
- **Branch**: ${env.BRANCH_NAME}
- **Commit**: ${GIT_COMMIT_SHORT}
- **Docker Image**: ${DOCKER_IMAGE} (pushed to registry)

Details:
- Checkout: Successful
- Build & Scan: ${buildStatus == 'SUCCESS' ? 'Passed' : 'Failed (check logs below)'}
- Push: ${buildStatus == 'SUCCESS' ? 'Successful' : 'Skipped (due to earlier failure)'}

"""
						if (buildStatus != 'SUCCESS') {
							def logs = ''
							try {
								logs = currentBuild.rawBuild.getLog(1000).join('\n')
							} catch (Exception logEx) {
								logs = "Unable to retrieve logs: ${logEx.message}"
							}
							message += """
**Error Logs (truncated):**
For full logs, contact the Jenkins admin.
"""
						} else {
							message += "All stages passed—no issues detected."
						}

						pullRequest.comment(message)
					} catch (Exception e) {
						echo "Failed to post PR comment: ${e.message}"
					}
				}
			}
			cleanWs()
		}
	}
}
