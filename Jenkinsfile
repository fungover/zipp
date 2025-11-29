pipeline {
	agent any
	tools {
		maven 'Maven3.9.9'
		jdk 'jdk-25'
	}
	environment {
		DOCKER_REGISTRY = '192.168.0.82:5000'
		APP_NAME = 'zipp'
		GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
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
		SPRING_MYSQL_DATABASE = 'appdb'
		DOMAIN = 'zipp.city'
		CLUSTER_ISSUER = 'letsencrypt-prod'
		KAFKA_BOOTSTRAP_SERVERS = 'my-kafka-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092'
	}
	stages {
		stage('Checkout') {
			steps {
				checkout scm
				publishChecks name: 'Deployment', title: 'Starting deployment', status: 'QUEUED', summary: 'Deployment pipeline initiated'
			}
		}
		stage('Verify Dockerfile') {
			steps {
				script {
					if (!fileExists('Dockerfile')) error 'Dockerfile not found'
				}
			}
		}
		stage('Build Docker Image') {
			steps {
				publishChecks name: 'Deployment', title: 'Building image', status: 'IN_PROGRESS', summary: 'Docker build in progress'
				echo 'Building Docker image from merged code...'
				sh 'mvn clean package -DskipTests'
				sh 'docker build -t ${DOCKER_IMAGE} --no-cache -f Dockerfile .'
			}
		}
		stage('Scan Image for Vulnerabilities') {
			steps {
				publishChecks name: 'Deployment', title: 'Scanning image', status: 'IN_PROGRESS', summary: 'Vulnerability scan in progress'
				sh 'trivy image --exit-code 1 --no-progress --severity HIGH,CRITICAL ${DOCKER_IMAGE}'
			}
		}
		stage('Push Docker Image') {
			when {
				branch 'main'  // Only on main (post-merge)
			}
			steps {
				publishChecks name: 'Deployment', title: 'Pushing image', status: 'IN_PROGRESS', summary: 'Docker push in progress'
				sh '''
                    docker push ${DOCKER_IMAGE}
                    docker tag ${DOCKER_IMAGE} ${DOCKER_IMAGE_LATEST}
                    docker push ${DOCKER_IMAGE_LATEST}
                '''
			}
		}
		stage('Generate Kubernetes Manifests') {
			when {
				branch 'main'
			}
			steps {
				publishChecks name: 'Deployment', title: 'Generating manifests', status: 'IN_PROGRESS', summary: 'k8s manifest generation in progress'
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
          value: jdbc:mysql://mycluster.mysql.svc.cluster.local:3306/${SPRING_MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true
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
				publishChecks name: 'Deployment', title: 'Deploying to k8s', status: 'IN_PROGRESS', summary: 'k8s deployment in progress'
				withCredentials([sshUserPrivateKey(credentialsId: SSH_CREDENTIALS_ID, keyFileVariable: 'SSH_KEY')]) {
					sh '''
                        ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${SSH_USER}@${CONTROL_PLANE_IP} "
                            mkdir -p ${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/{deployments,services,hpas,ingresses}
                        "
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/deployments/deployment.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/deployments/
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/services/service.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/services/
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/hpas/hpa.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/hpas/
                        scp -i ${SSH_KEY} ${K8S_MANIFEST_DIR}/ingresses/ingress.yaml ${SSH_USER}@${CONTROL_PLANE_IP}:${K8S_MANIFEST_PATH}/${K8S_MANIFEST_DIR}/ingresses/
                        ssh -i ${SSH_KEY} ${SSH_USER}@${CONTROL_PLANE_IP} "
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
			publishChecks name: 'Deployment', title: 'Deployment complete', status: 'COMPLETED', conclusion: currentBuild.resultIsBetterOrEqualTo('SUCCESS') ? 'SUCCESS' : 'FAILURE', summary: "Deployment ${currentBuild.currentResult}", detailsURL: env.BUILD_URL
			cleanWs()
		}
	}
}
