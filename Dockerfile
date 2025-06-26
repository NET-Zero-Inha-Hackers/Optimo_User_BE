# OpenJDK 17 기반 이미지
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사 (Gradle 기준)
COPY build/libs/*.jar app.jar

# 타임존 설정 (선택)
ENV TZ=Asia/Seoul

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]