FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY target/user-service-1.0.0-SNAPSHOT.jar user-service.jar

ENTRYPOINT ["java", "-jar", "user-service.jar"]

EXPOSE 8082
