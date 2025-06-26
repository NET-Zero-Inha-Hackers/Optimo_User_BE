package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.EmailRequest;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

@RequiredArgsConstructor
public class UserFunction {

    private static final ThreadLocal<ApplicationContext> contextHolder =
            ThreadLocal.withInitial(() ->
                    new AnnotationConfigApplicationContext("org.inhahackers.optmo_user_be")
            );

    @FunctionName("userFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "user",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Void> request,
            final ExecutionContext executionContext) {

        executionContext.getLogger().info("Processing userFunction request");

        try {
            // 요청 바디 파싱
            ApplicationContext context = contextHolder.get();
            JwtTokenService jwtTokenService = context.getBean(JwtTokenService.class);
            UserService userService = context.getBean(UserService.class);

            String email = request.getQueryParameters().get("email");

            // 유저 정보 조회 및 생성
            var user = userService.findOrCreateUserByEmail(email);

            // JWT 토큰 생성 (필요하다면 갱신용)
            String newToken = jwtTokenService.generateToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()
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

            // 응답 헤더에 Authorization 토큰 포함
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
            executionContext.getLogger().severe("ERROR: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage())
                    .build();
        }
    }
}