package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.OAuthUserInfo;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.entity.AuthProvider;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.InvalidAuthorizationHeaderException;
import org.inhahackers.optmo_user_be.exception.OAuthTokenValidationException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.OAuthTokenService;
import org.inhahackers.optmo_user_be.service.UserService;

import java.util.Optional;

@RequiredArgsConstructor
public class OAuthUserFunction {

    private final UserService userService;
    private final OAuthTokenService tokenService;
    private final JwtTokenService jwtTokenService;

    @FunctionName("oauthUserFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "oauthuser",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("oauthUserFunction called");

        try {
            // 1. Authorization 헤더 추출 및 검증
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing or invalid Authorization header")
                        .build();
            }
            String accessToken = authHeader.substring("Bearer ".length());

            // 2. provider 쿼리 파라미터 추출 및 검증
            String providerStr = request.getQueryParameters().get("provider");
            if (providerStr == null || providerStr.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing provider query parameter")
                        .build();
            }

            AuthProvider provider;
            try {
                provider = AuthProvider.valueOf(providerStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Invalid provider parameter")
                        .build();
            }

            // 3. 토큰 검증 및 사용자 정보 조회
            OAuthUserInfo userInfo = tokenService.verifyAndGetUserInfo(accessToken, provider);
            User user = userService.findOrCreateUser(userInfo.toUserOAuthRequest());

            // 4. JWT 토큰 생성
            String jwtToken = jwtTokenService.generateToken(
                    user.getId(), user.getEmail(), user.getRole().name());

            // 5. 응답 DTO 생성
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .build();

            // 6. 응답 반환 (헤더에 Authorization 포함)
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(response)
                    .build();

        } catch (InvalidAuthorizationHeaderException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid Authorization header: " + e.getMessage())
                    .build();
        } catch (OAuthTokenValidationException e) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .body("Token verification failed: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage())
                    .build();
        }
    }
}