package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component("userFunction")
public class UserFunction implements Function<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    public UserFunction(JwtTokenService jwtTokenService, UserService userService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    @Override
    public HttpResponseMessage apply(HttpRequestMessage<Optional<String>> request) {
        try {
            // 쿼리 파라미터에서 email 추출
            String email = request.getQueryParameters().get("email");

            if (email == null || email.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Email parameter is required")
                        .build();
            }

            // 유저 조회 또는 생성
            var user = userService.findOrCreateUserByEmail(email);

            // JWT 발급
            String newToken = jwtTokenService.generateToken(
                    user.getId(), user.getEmail(), user.getRole().name()
            );

            // 응답 DTO 생성
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

            // 응답 반환
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + newToken)
                    .body(response)
                    .build();

        } catch (JwtAuthenticationException e) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage())
                    .build();
        }
    }
}