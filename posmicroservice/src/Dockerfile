# It Worked
# Multi-stage build
# Stage 1: Build the application
# Use official lightweight JDK 21 base image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
# The JAR should be built by Maven before Docker build
COPY target/posmicroservice-0.0.1-SNAPSHOT.jar app.jar

# Expose the application's port (8081)
EXPOSE 8088

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]