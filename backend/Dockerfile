# This Dockerfile is a placeholder until the project is ready for a real dockerbuild

FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
