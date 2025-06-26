package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.ElecRequest;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;

import java.util.Optional;

@RequiredArgsConstructor
public class IncreaseElecFunction {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @FunctionName("increaseElecFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "increaseelec",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("increaseElecFunction called");

        try {
            // 1. Authorization 헤더 추출
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing or invalid Authorization header")
                        .build();
            }
            String accessToken = authHeader.substring("Bearer ".length());

            // 2. Request Body 파싱
            ElecRequest elecRequest = ElecRequest.builder()
                    .useElecEstimate(Long.getLong(request.getQueryParameters().get("useElecEstimate")))
                    .llmElecEstimate(Long.getLong(request.getQueryParameters().get("llmElecEstimate")))
                    .build();

            // 3. 토큰에서 userId 추출 및 처리
            Long userId = jwtTokenService.extractUserId(accessToken);

            userService.increaseElecEstimate(userId, elecRequest);

            // 4. 성공 응답
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "text/plain")
                    .body("Successfully Increase Elec and Cost Estimate")
                    .build();

        } catch (JwtAuthenticationException e) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage())
                    .build();

        } catch (UserNotFoundException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("User Not Found: " + e.getMessage())
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