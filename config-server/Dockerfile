FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install git for config server (needed for local git repo)
RUN apk add --no-cache git

# Initialize Git configuration repository
RUN mkdir -p /tmp/config-repo && \
    cd /tmp/config-repo && \
    git init && \
    git config user.email "config@localhost" && \
    git config user.name "Config Server" && \
    echo "# Config Repository" > README.md && \
    git add README.md && \
    git commit -m "Initial config repository"

COPY target/config-server-1.0.0-SNAPSHOT.jar config-server.jar

# Optional: copy config files to the config directory
# COPY config/ /app/config/

ENTRYPOINT ["java", "-jar", "config-server.jar"]

EXPOSE 8888
