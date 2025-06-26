# Optimo_User_BE
Optimo 프로젝트의 User 조회 서비스 레포지토리입니다.

## 서버리스를 고려한 Spring Function Project입니다.
- 해당 서비스는 3rd Party Oauth2.0과 자체 발급 Jwt로 검증 후, User 정보를 조회하는 서비스입니다.
- 아래와 같은 경로로 URL 요청을 할 경우, User 정보를 반환합니다.

---
# 📘 Optmo User 서비스 API 명세서

### 1. OAuth 사용자 인증 및 회원가입
**Endpoint**: `POST /api/oauthUser`  
**Handler**: `OAuthUserFunction`  
**기능**: OAuth 제공자(Provider)를 통한 사용자 인증/회원가입 및 JWT 발급

#### 📤 요청
- **Headers**:
    - `Authorization`: `Bearer {OAuth_Access_Token}`
- **Query Parameters**:
    - `provider`: OAuth 제공자 (e.g., `google`, `kakao`)

#### 📥 응답
- **성공 시 (200 OK)**:
    - **Headers**: `Authorization: Bearer {JWT_Token}`
    - **Body** (JSON):
      ```
      {
        "id": 123,
        "email": "user@example.com",
        "name": "홍길동",
        "profileImage": "https://...",
        "provider": "GOOGLE",
        "totalUseElecEstimate": 150.5,
        "totalLlmElecEstimate": 30.2,
        "totalUseCostEstimate": 45000,
        "totalLlmCostEstimate": 9000
      }
      ```
- **에러**:
    - `400 Bad Request`: Provider 파라미터 누락/유효하지 않음
    - `401 Unauthorized`: OAuth 토큰 검증 실패
    - `500 Internal Server Error`: 서버 내부 오류

---

### 2. JWT 사용자 정보 조회
**Endpoint**: `POST /api/user`  
**Handler**: `JWTUserFunction`  
**기능**: JWT 토큰으로 사용자 정보 조회

#### 📤 요청
- **Headers**:
    - `Authorization`: `Bearer {JWT_Token}`

#### 📥 응답
- **성공 시 (200 OK)**:
    - **Body** (JSON):
      ```
      {
        "id": 123,
        "email": "user@example.com",
        "name": "홍길동",
        "profileImage": "https://...",
        "provider": "GOOGLE",
        "totalUseElecEstimate": 150.5,
        "totalLlmElecEstimate": 30.2,
        "totalUseCostEstimate": 45000,
        "totalLlmCostEstimate": 9000
      }
      ```
- **에러**:
    - `400 Bad Request`: Authorization 헤더 오류
    - `401 Unauthorized`: JWT 검증 실패
    - `404 Not Found`: 사용자 없음 (코드상에선 500으로 처리됨)
    - `500 Internal Server Error`: 서버 내부 오류

---

### 3. 전력량/비용 예상치 증가
**Endpoint**: `PATCH /api/elecAndCost`  
**Handler**: `IncreaseElecAndCostFunction`  
**기능**: 사용자의 전력량/비용 예상치 증가 (⚠️ 라우터 버그 주의: 현재 `handleJWTUser` 매핑됨)

#### 📤 요청
- **Headers**:
    - `Authorization`: `Bearer {JWT_Token}`
- **Body** (JSON):
```
{
    "useElecEstimate": 5000,
    "llmElecEstimate": 5000,
    "useCostEstimate": 5000,
    "llmCostEstimate": 5000
}
```

#### 📥 응답
- **성공 시 (200 OK)**:
- **Body**: `"Successfully Increase Elec and Cost Estimate"`
- **에러**:
- `400 Bad Request`: Authorization 헤더 오류
- `401 Unauthorized`: JWT 검증 실패
- `404 Not Found`: 사용자 없음 (코드상에선 500으로 처리됨)
- `500 Internal Server Error`: 서버 내부 오류

---

## ⚠️ 주의사항
1. **보안**:
- 모든 엔드포인트는 `Authorization` 헤더 필수
- JWT 토큰은 `jwtTokenService`에서 검증

2. **에러 처리**:
- 비즈니스 로직 오류는 `500 Internal Server Error`로 통일
- 구체적인 오류 메시지는 응답 body에 포함



---


# 🛠️ 사용 흐름 요약
1. FE에서 Google/Kakao 로그인 후 access_token 확보
2. /api/oauthUser?provider=google에 access_token 전송
3. 백엔드가 토큰 검증 후 유저 정보 저장 및 JWT 발급
4. FE는 응답 헤더의 JWT를 저장 (ex. localStorage)
5. 이후 요청에서 JWT를 Authorization 헤더에 담아 사용

