FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/notification-service-1.0.0-SNAPSHOT.jar notification-service.jar
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
EXPOSE 8086
