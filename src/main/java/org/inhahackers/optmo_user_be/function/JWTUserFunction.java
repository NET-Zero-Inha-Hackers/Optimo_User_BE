package org.inhahackers.optmo_user_be.function;

import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.InvalidAuthorizationHeaderException;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
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
public class JWTUserFunction implements Function<ServerRequest, Mono<ServerResponse>> {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Override
    public Mono<ServerResponse> apply(ServerRequest request) {
        // 1. Authorization 헤더 추출
        String accessToken;
        try {
            accessToken = AuthorizationHeaderUtil.extractToken(request.headers());
            // 이후 처리
        } catch (InvalidAuthorizationHeaderException e) {
            return ServerResponse.badRequest().bodyValue(e.getMessage());
        }

        try {
            // 2. 검증 및 정보 추출
            Long userId = jwtTokenService.extractUserId(accessToken);
            String email = jwtTokenService.extractEmail(accessToken);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));

            // 3. 응답 DTO 구성 (유저 생성 X)
            UserResponse response = UserResponse.builder()
                    .id(userId)
                    .email(email)
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);

        } catch (JwtAuthenticationException e) {
            return ServerResponse.status(401)
                    .bodyValue("Invalid JWT: " + e.getMessage());
        } catch (Exception e) {
            return ServerResponse.status(500)
                    .bodyValue("Internal error: " + e.getMessage());
        }
    }
}
