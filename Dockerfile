# Use an official OpenJDK runtime as a base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew gradlew.bat build.gradle settings.gradle /app/
COPY gradle /app/gradle/

# Copy the application source code
COPY src /app/src/

# Install dos2unix to fix line endings
RUN apt-get update && apt-get install -y dos2unix

# Ensure the Gradle wrapper has proper line endings and is executable
RUN dos2unix gradlew && chmod +x gradlew

# Build the application
RUN ./gradlew bootJar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "build/libs/WatchItNow-0.0.1-SNAPSHOT.jar"]
