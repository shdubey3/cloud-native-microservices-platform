FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY target/order-service-1.0.0-SNAPSHOT.jar order-service.jar

ENTRYPOINT ["java", "-jar", "order-service.jar"]

EXPOSE 8083
