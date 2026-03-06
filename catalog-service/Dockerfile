FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/catalog-service-1.0.0-SNAPSHOT.jar catalog-service.jar
ENTRYPOINT ["java", "-jar", "catalog-service.jar"]
EXPOSE 8084
