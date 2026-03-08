FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/recommendation-service-1.0.0-SNAPSHOT.jar recommendation-service.jar
ENTRYPOINT ["java", "-jar", "recommendation-service.jar"]
EXPOSE 8085
