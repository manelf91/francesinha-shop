# Build stage
FROM maven:3.9.9-eclipse-temurin-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package spring-boot:repackage -DskipTests

# Runtime stage
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
