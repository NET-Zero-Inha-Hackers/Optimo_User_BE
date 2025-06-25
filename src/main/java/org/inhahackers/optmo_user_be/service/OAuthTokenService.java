package org.inhahackers.optmo_user_be.service;

import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.OAuthUserInfo;
import org.inhahackers.optmo_user_be.entity.AuthProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import org.inhahackers.optmo_user_be.exception.OAuthTokenValidationException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final WebClient webClient;

    public OAuthUserInfo verifyAndGetUserInfo(String accessToken, AuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> getGoogleUserInfo(accessToken);
            case KAKAO -> getKakaoUserInfo(accessToken);
        };
    }

    private OAuthUserInfo getGoogleUserInfo(String token) {
        try {
            return webClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new OAuthTokenValidationException("Google token verification failed: " + errorBody)))
                    )
                    .bodyToMono(Map.class)
                    .map(body -> OAuthUserInfo.builder()
                            .email((String) body.get("email"))
                            .name((String) body.get("name"))
                            .profileImage((String) body.get("picture"))
                            .providerId((String) body.get("id"))
                            .provider(AuthProvider.GOOGLE)
                            .build())
                    .block();
        } catch (Exception e) {
            throw new OAuthTokenValidationException("Google token verification error: " + e.getMessage(), e);
        }
    }

    private OAuthUserInfo getKakaoUserInfo(String token) {
        try {
            return webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new OAuthTokenValidationException("Kakao token verification failed: " + errorBody)))
                    )
                    .bodyToMono(Map.class)
                    .map(body -> {
                        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
                        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                        return OAuthUserInfo.builder()
                                .email((String) kakaoAccount.get("email"))
                                .name((String) profile.get("nickname"))
                                .profileImage((String) profile.get("profile_image_url"))
                                .providerId(String.valueOf(body.get("id")))
                                .provider(AuthProvider.KAKAO)
                                .build();
                    })
                    .block();
        } catch (Exception e) {
            throw new OAuthTokenValidationException("Kakao token verification error: " + e.getMessage(), e);
        }
    }
}