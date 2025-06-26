package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.EmailRequest;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;

import java.util.Optional;

@RequiredArgsConstructor
public class UserFunction {

    private JwtTokenService jwtTokenService;
    private UserService userService;

    @FunctionName("userFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    dataType = "application/json") HttpRequestMessage<Optional<EmailRequest>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing userFunction request");

        try {
            // 1. Authorization 헤더 추출
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid Authorization header")
                        .build();
            }
            String token = authHeader.substring("Bearer ".length());

            // 2. 요청 바디 파싱
            EmailRequest emailRequest = request.getBody()
                    .orElseThrow(() -> new IllegalArgumentException("EmailRequest body is missing"));

            // 3. 유저 정보 조회 및 생성
            var user = userService.findOrCreateUserByEmail(emailRequest.getEmail());

            // 4. JWT 토큰 생성 (필요하다면 갱신용)
            String newToken = jwtTokenService.generateToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()
            );

            // 5. 응답 DTO 생성
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

            // 6. 응답 헤더에 Authorization 토큰 포함
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + newToken)
                    .body(response)
                    .build();

        } catch (JwtAuthenticationException e) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage())
                    .build();

        } catch (IllegalArgumentException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage())
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage())
                    .build();
        }
    }
}