package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component("jwtUserFunction")
@RequiredArgsConstructor
public class JWTUserFunction implements Function<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Override
    public HttpResponseMessage apply(HttpRequestMessage<Optional<String>> request) {

        try {
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing or invalid Authorization header")
                        .build();
            }
            String accessToken = authHeader.substring("Bearer ".length());

            Long userId = jwtTokenService.extractUserId(accessToken);
            String email = jwtTokenService.extractEmail(accessToken);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));

            UserResponse response = UserResponse.builder()
                    .id(userId)
                    .email(email)
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

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