# Optimo_User_BE
Optimo 프로젝트의 User 조회 서비스 레포지토리입니다.

## 서버리스를 고려한 Spring Function Project입니다.
- 해당 서비스는 3rd Party Oauth2.0과 자체 발급 Jwt로 검증 후, User 정보를 조회하는 서비스입니다.
- 아래와 같은 경로로 URL 요청을 할 경우, User 정보를 반환합니다.

---
# 📘 Optmo User 조회 API 명세서
## 🔐 공통사항
- 모든 요청은 `JSON` 기반
- 인증 토큰은 `Authorization`: `Bearer <token>` 형식으로 전송
- 응답 타입: `application/json`

## 1. 🔑 OAuth 사용자 등록 및 JWT 발급
### ✅ POST /api/oauthUser
- 설명: <br>
   OAuth provider(Google/Kakao)로부터 받은 액세스 토큰을 검증하고,
   유저를 DB에 등록한 후 자체 JWT 토큰을 응답 헤더에 포함하여 반환합니다.
---
- 📥 Request
  - Headers
    ```
    Authorization: Bearer {oauth-access-token}
    ```
  - Query Parameters
      ```angular2html
      provider=google | kakao
      ```
- 📤 Response
  - ✅ 200 OK
    - Headers
      ```
      Authorization: Bearer {jwt-token}
      ```
    - Body
      ```
      {
          "id": 1,
          "email": "user@example.com",
          "name": "홍길동",
          "profileImage": "https://...",
          "provider": "GOOGLE"
      }
      ```


- ❌ 400 Bad Request : 쿼리 파라미터나 헤더가 잘못되었을 때
- ❌ 401 Unauthorized : OAuth 토큰이 유효하지 않을 때

## 2. 🙍 JWT로 사용자 정보 요청
### ✅ POST /api/user
- 설명: <br>
   FE가 저장한 JWT를 Authorization 헤더에 담아 사용자 정보를 요청합니다.

- 📥 Request
  - Headers
    ```
    Authorization: Bearer {jwt-token}
    ```
- 📤 Response
  - ✅ 200 OK
    - Body
      ```
      {
          "id": 1,
          "email": "user@example.com",
          "name": "홍길동",
          "profileImage": "https://...",
          "provider": "GOOGLE"
      }
      ```

  - ❌ 400 Bad Request : Authorization 헤더 누락/형식 오류
  - ❌ 401 Unauthorized : JWT가 만료되었거나 위조된 경우
  - ❌ 500 Internal Server Error : 내부 서버 오류

---
# 🛠️ 사용 흐름 요약
1. FE에서 Google/Kakao 로그인 후 access_token 확보
2. /api/oauthUser?provider=google에 access_token 전송
3. 백엔드가 토큰 검증 후 유저 정보 저장 및 JWT 발급
4. FE는 응답 헤더의 JWT를 저장 (ex. localStorage)
5. 이후 요청에서 JWT를 Authorization 헤더에 담아 사용

