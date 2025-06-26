package org.inhahackers.optmo_user_be.function;

import com.microsoft.azure.functions.*;
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
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component("oauthUserFunction")
@RequiredArgsConstructor
public class OAuthUserFunction implements Function<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    private final UserService userService;
    private final OAuthTokenService tokenService;
    private final JwtTokenService jwtTokenService;

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

            OAuthUserInfo userInfo = tokenService.verifyAndGetUserInfo(accessToken, provider);
            User user = userService.findOrCreateUser(userInfo.toUserOAuthRequest());

            String jwtToken = jwtTokenService.generateToken(
                    user.getId(), user.getEmail(), user.getRole().name());

            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .build();

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