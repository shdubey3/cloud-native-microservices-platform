FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/gateway-service-1.0.0-SNAPSHOT.jar gateway-service.jar

ENTRYPOINT ["java", "-jar", "gateway-service.jar"]

EXPOSE 8080
