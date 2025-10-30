# Multi-Stage Build 시작: Builder 단계

# 1단계: 빌드 환경 (Java 17 JDK)
FROM eclipse-temurin:17-jdk-focal as builder
WORKDIR /app

# ⬇️⬇️ [수정!] Maven 빌드 파일들을 복사합니다. ⬇️⬇️
# pom.xml, mvnw, .mvn 폴더를 복사합니다.
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 소스 코드를 복사합니다.
COPY src src

# JAR 파일을 빌드합니다. (Maven 명령어 사용)
# RUN ./mvnw clean package -DskipTests 는 target/*.jar로 빌드합니다.
RUN ./mvnw clean package -DskipTests

# -----------------------------------------------------------------

# 2단계: 실행 환경 (Java 17 JRE)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 1단계에서 빌드된 JAR 파일을 실행 환경으로 복사합니다. (Maven은 target 폴더에 생성)
COPY --from=builder /app/target/*.jar app.jar

# 컨테이너 시간대를 서울로 설정합니다.
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 서버 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]