# Multi-Stage Build 시작: Builder 단계

# 1단계: 빌드 환경 (Java 17 JDK)
# eclipse-temurin 이미지는 Docker 환경에서 Spring Boot 앱을 빌드하는 데 적합합니다.
FROM eclipse-temurin:17-jdk-focal as builder
WORKDIR /app

# Gradle Wrapper 파일들을 복사합니다.
# 이 파일들은 빌드 명령에 필요합니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Windows에서 개발했을 경우를 대비하여 권한을 부여합니다.
RUN chmod +x ./gradlew

# 모든 소스 코드를 복사합니다. (application-prod.yml, WebConfig 등 포함)
COPY src src

# JAR 파일을 빌드합니다.
# 빌드 시 필요한 의존성 다운로드 및 JAR 파일 생성
RUN ./gradlew bootJar --no-daemon

# -----------------------------------------------------------------

# 2단계: 실행 환경 (Java 17 JRE)

# JRE만 포함된 경량 이미지를 사용합니다. (최종 이미지 크기 대폭 감소)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 1단계에서 빌드된 JAR 파일을 실행 환경으로 복사합니다.
# JAR 파일은 build/libs 폴더에 생성됩니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 시간대를 서울로 설정합니다. (선택사항)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 서버 실행 명령어 (컨테이너 실행 시 자동 실행)
ENTRYPOINT ["java", "-jar", "app.jar"]