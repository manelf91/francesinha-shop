# Use Maven + JDK 24 as base image
FROM maven:3.9-eclipse-temurin-24

# Set working directory
WORKDIR /app

# Copy pom and source files
COPY pom.xml .
COPY src ./src

# Run E2E tests when container starts
CMD mvn verify
