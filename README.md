# Optimo_User_BE
Optimo 프로젝트의 User 조회 서비스 레포지토리입니다.

## 서버리스를 고려한 Spring Function Project입니다.
- 해당 서비스는 3rd Party Oauth2.0과 자체 발급 Jwt로 검증 후, User 정보를 조회하는 서비스입니다.
- 아래와 같은 경로로 URL 요청을 할 경우, User 정보를 반환합니다.

---
# BE Server 테스트 및 실행

## 환경

- JDK 17, SpringBoot 3.5, Java + Gradle

## Connected Infra

- PostgeSQL
  - 환경 변수
    - `POSTGRES_USERNAME` : PostgreSQL ID
    - `POSTGRES_PASSWORD` : PostgreSQL PW

## 실행 방법
```
# 이전 빌드 제거 및 빌드 명령어
./gradlew clean bootJar

# 빌드된 jar 파일 실행 명령어
java -jar ./build/libs/Optimo_User_BE-0.0.1-SNAPSHOT.jar

```


---

