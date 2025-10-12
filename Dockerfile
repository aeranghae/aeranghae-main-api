# Dockerfile

# 1. 빌드 스테이지 (Build Stage)
# 애플리케이션을 빌드하기 위한 환경
FROM eclipse-temurin:21-jdk-jammy AS builder

# 작업 디렉터리 설정
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 의존성 다운로드를 위해 더미 빌드 실행 (레이어 캐싱 활용)
RUN ./gradlew dependencies

# 소스 코드 복사
COPY src src

# 실제 빌드 실행 (JAR 파일 생성)
RUN ./gradlew bootJar

# 2. 실행 스테이지 (Run Stage)
# 애플리케이션 실행에 필요한 최소한의 환경
# JDK 대신 JRE를 사용하여 이미지 크기 대폭 감소
FROM eclipse-temurin:21-jre-jammy AS runner

# 타임존 설정 (선택 사항)
ENV TZ=Asia/Seoul

# 빌드 스테이지에서 생성된 JAR 파일 복사
# build/libs/aeranghae-main-api-0.0.1-SNAPSHOT.jar 경로를 프로젝트에 맞게 확인 및 수정하세요.
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 시작 시 실행될 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]