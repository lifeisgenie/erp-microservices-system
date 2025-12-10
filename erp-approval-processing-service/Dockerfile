# ============================
# Build stage
# ============================
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle* ./
COPY src ./src

RUN chmod +x gradlew && \
    ./gradlew clean bootJar --no-daemon

# ============================
# Runtime stage
# ============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

# 8080: REST(/process), 9090: gRPC(RequestApproval)
EXPOSE 8080 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]