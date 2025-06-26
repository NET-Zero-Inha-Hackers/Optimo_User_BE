package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.ElecRequest;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component("increaseElecFunction")
@RequiredArgsConstructor
public class IncreaseElecFunction implements Function<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Override
    public HttpResponseMessage apply(HttpRequestMessage<Optional<String>> request) {
        try {
            // 1. Authorization 헤더 추출
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing or invalid Authorization header")
                        .build();
            }
            String accessToken = authHeader.substring("Bearer ".length());

            // 2. 쿼리 파라미터에서 long 값 안전하게 파싱
            String useElecStr = request.getQueryParameters().get("useElecEstimate");
            String llmElecStr = request.getQueryParameters().get("llmElecEstimate");

            if (useElecStr == null || llmElecStr == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing required query parameters: useElecEstimate and llmElecEstimate")
                        .build();
            }

            long useElecEstimate;
            long llmElecEstimate;
            try {
                useElecEstimate = Long.parseLong(useElecStr);
                llmElecEstimate = Long.parseLong(llmElecStr);
            } catch (NumberFormatException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Invalid number format in query parameters")
                        .build();
            }

            ElecRequest elecRequest = ElecRequest.builder()
                    .useElecEstimate(useElecEstimate)
                    .llmElecEstimate(llmElecEstimate)
                    .build();

            // 3. 토큰에서 userId 추출 및 처리
            Long userId = jwtTokenService.extractUserId(accessToken);

            userService.increaseElecEstimate(userId, elecRequest);

            // 4. 성공 응답
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "text/plain")
                    .body("Successfully Increased Elec and Cost Estimate")
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