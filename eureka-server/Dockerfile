FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY target/eureka-server-1.0.0-SNAPSHOT.jar eureka-server.jar

ENTRYPOINT ["java", "-jar", "eureka-server.jar"]

EXPOSE 8761
