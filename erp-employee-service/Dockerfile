# ============================
# Build stage
# ============================
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

# Gradle wrapper 및 설정 파일 복사
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

# 빌드 스테이지에서 생성된 jar 복사
COPY --from=build /workspace/build/libs/*.jar app.jar

# REST 포트
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]