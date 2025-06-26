package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class JWTUserFunction {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @FunctionName("jwtUserFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("jwtUserFunction called");

        try {
            // 1. Authorization 헤더 추출
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing or invalid Authorization header")
                        .build();
            }
            String accessToken = authHeader.substring("Bearer ".length());

            // 2. 토큰에서 사용자 정보 추출
            Long userId = jwtTokenService.extractUserId(accessToken);
            String email = jwtTokenService.extractEmail(accessToken);

            // 3. 사용자 조회
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));

            // 4. 응답 DTO 생성
            UserResponse response = UserResponse.builder()
                    .id(userId)
                    .email(email)
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

            // 5. 응답 반환 (JSON + 200 OK)
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();

        } catch (JwtAuthenticationException e) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage())
                    .build();

        } catch (UserNotFoundException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("User Not Found: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage())
                    .build();
        }
    }
}