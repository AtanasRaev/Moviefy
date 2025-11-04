# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM gradle:8.10-jdk17-alpine AS build
WORKDIR /app

# Copy wrapper and build files first to leverage cache
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./

# Ensure gradlew uses LF and is executable (Windows-safe)
RUN apk add --no-cache dos2unix \
 && dos2unix gradlew \
 && chmod +x gradlew \
 && ./gradlew --version

# Copy sources and build
COPY src src
# Skip tests if you want faster/cleaner image builds:
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
