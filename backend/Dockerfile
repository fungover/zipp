FROM openjdk:25-jdk-slim

WORKDIR /app

COPY backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
