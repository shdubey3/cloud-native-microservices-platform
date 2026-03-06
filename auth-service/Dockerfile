FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/auth-service-1.0.0-SNAPSHOT.jar auth-service.jar

ENTRYPOINT ["java", "-jar", "auth-service.jar"]

EXPOSE 8081
