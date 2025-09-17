FROM maven:3.9.9-eclipse-temurin-24

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install dockerize
RUN wget https://github.com/jwilder/dockerize/releases/download/v0.6.1/dockerize-linux-amd64-v0.6.1.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-v0.6.1.tar.gz \
    && rm dockerize-linux-amd64-v0.6.1.tar.gz

CMD dockerize \
      -wait tcp://auth:8080 -timeout 60s \
      -wait tcp://products:8081 -timeout 60s \
      -wait tcp://reviews:8082 -timeout 60s \
    && mvn verify