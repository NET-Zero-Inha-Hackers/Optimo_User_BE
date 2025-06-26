package org.inhahackers.optmo_user_be.function;

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
import org.inhahackers.optmo_user_be.util.AuthorizationHeaderUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class OAuthUserFunction implements Function<ServerRequest, Mono<ServerResponse>> {

    private final UserService userService;
    private final OAuthTokenService tokenService;
    private final JwtTokenService jwtTokenService;

    @Override
    public Mono<ServerResponse> apply(ServerRequest request) {
        // 1. Authorization 헤더 추출 및 검증
        String accessToken;
        try {
            accessToken = AuthorizationHeaderUtil.extractToken(request.headers());
            // 이후 처리
        } catch (InvalidAuthorizationHeaderException e) {
            return ServerResponse.badRequest().bodyValue(e.getMessage());
        }

        // 2. provider 쿼리 파라미터 추출 및 검증
        String providerStr = request.queryParam("provider").orElse("");
        AuthProvider provider;
        try {
            provider = AuthProvider.valueOf(providerStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid or missing provider parameter");
        }

        // 3. 토큰 검증 및 사용자 정보 조회
        try {
            OAuthUserInfo userInfo = tokenService.verifyAndGetUserInfo(accessToken, provider);
            User user = userService.findOrCreateUser(userInfo.toUserOAuthRequest());

            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .build();

            // JWT 토큰 생성 (예: email과 role 기반)
            String jwtToken = jwtTokenService.generateToken(
                    user.getId(), user.getEmail(), user.getRole().name());

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(response);

        } catch (OAuthTokenValidationException e) {
            return ServerResponse.status(401)
                    .bodyValue("Token verification failed: " + e.getMessage());
        } catch (Exception e) {
            return ServerResponse.status(500)
                    .bodyValue("Internal server error: " + e.getMessage());
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .build();
    }
}
