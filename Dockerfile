# Step 1: Build Stage
FROM openjdk:17-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app/repo

# Gradle 캐시 활용을 위해 빌드 스크립트 먼저 복사
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Gradle 의존성 캐시 구축
RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon --refresh-dependencies -x test || return 0

# 소스 코드 복사
COPY src ./src

# Gradle 빌드 실행
RUN ./gradlew clean build --no-daemon -x test

# Step 2: Production Stage
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app/repo

# 빌드된 JAR 파일 복사
COPY --from=build /app/repo/build/libs/*.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]