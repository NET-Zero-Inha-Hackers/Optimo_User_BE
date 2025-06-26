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
# Optmo User API 명세서

## 개요

- **제목:** Optmo User API
- **버전:** 1.0.0
- **설명:**
사용자 관리 및 전기 사용량 추적 API


## API 목록

### 1. 이메일로 사용자 조회/생성

**URL:** `/api/user`
**Method:** `GET`
**요청 파라미터:**


| 파라미터 | 타입 | 필수 | 설명 |
| :-- | :-- | :-- | :-- |
| email | String | O | 사용자 이메일 |

**요청 헤더:**

- `Authorization: Bearer [JWT 토큰]`

**응답:**

- **성공 (200):**
    - **헤더:**
        - `Authorization: Bearer [JWT 토큰]`
    - **본문:**

```json
{
  "id": 12345,
  "email": "user@example.com",
  "name": "홍길동",
  "profileImage": "https://example.com/profile.jpg",
  "provider": "EMAIL",
  "totalUseElecEstimate": 1500,
  "totalLlmElecEstimate": 500
}
```

- **실패 (400):**
    - `Email parameter is required`
- **실패 (401):**
    - `Invalid JWT: [error details]`
- **실패 (500):**
    - `Internal error: [error details]`


### 2. OAuth로 사용자 조회/생성

**URL:** `/api/user/oauth`
**Method:** `POST`
**요청 파라미터:**


| 파라미터 | 타입 | 필수 | 설명 |
| :-- | :-- | :-- | :-- |
| provider | String | O | OAuth 제공자 (GOOGLE, KAKAO, NAVER 등) |

**요청 헤더:**

- `Authorization: Bearer [OAuth 토큰]`

**응답:**

- **성공 (200):**
    - **헤더:**
        - `Authorization: Bearer [JWT 토큰]`
    - **본문:**

```json
{
  "id": 12345,
  "email": "user@example.com",
  "name": "홍길동",
  "profileImage": "https://example.com/profile.jpg",
  "provider": "GOOGLE",
  "totalUseElecEstimate": 1500,
  "totalLlmElecEstimate": 500
}
```

- **실패 (401):**
    - `Invalid JWT: [error details]`
- **실패 (404):**
    - `User Not Found: [error details]`
- **실패 (500):**
    - `Internal error: [error details]`


### 3. JWT로 사용자 정보 조회

**URL:** `/api/user/jwt`
**Method:** `GET`
**요청 헤더:**

- `Authorization: Bearer [JWT 토큰]`

**응답:**

- **성공 (200):**
    - **본문:**

```json
{
  "id": 12345,
  "email": "user@example.com",
  "name": "홍길동",
  "profileImage": "https://example.com/profile.jpg",
  "provider": "EMAIL",
  "totalUseElecEstimate": 1500,
  "totalLlmElecEstimate": 500
}
```

- **실패 (401):**
    - `Invalid JWT: [error details]`
- **실패 (404):**
    - `User Not Found: [error details]`
- **실패 (500):**
    - `Internal error: [error details]`


### 4. 전기 사용량 업데이트

**URL:** `/api/user/electricity`
**Method:** `PATCH`
**요청 헤더:**

- `Authorization: Bearer [JWT 토큰]`

**요청 본문:**

```json
{
  "useElecEstimate": 100,
  "llmElecEstimate": 50
}
```

**응답:**

- **성공 (200):**
    - **본문:**

```
Successfully Increased Elec and Cost Estimate
```

- **실패 (401):**
    - `Invalid JWT: [error details]`
- **실패 (404):**
    - `User Not Found: [error details]`
- **실패 (500):**
    - `Internal error: [error details]`


## 데이터 모델

### UserResponse

| 필드 | 타입 | 예시 값 |
| :-- | :-- | :-- |
| id | Long | 12345 |
| email | String | user@example.com |
| name | String | 홍길동 |
| profileImage | String | https://example.com/profile.jpg |
| provider | String | EMAIL, GOOGLE, KAKAO, NAVER |
| totalUseElecEstimate | Long | 1500 |
| totalLlmElecEstimate | Long | 500 |

### ElecRequest

| 필드 | 타입 | 예시 값 |
| :-- | :-- | :-- |
| useElecEstimate | Long | 100 |
| llmElecEstimate | Long | 50 |

## 공통 헤더

- **Authorization:** Bearer 토큰 (JWT 또는 OAuth 토큰)


## 오류 코드

| 코드 | 설명 |
| :-- | :-- |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 404 | 리소스 없음 |
| 500 | 서버 내부 오류 |



---

