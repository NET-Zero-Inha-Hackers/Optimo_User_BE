package org.inhahackers.optmo_user_be.function;

import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.EmailRequest;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class UserFunction implements Function<ServerRequest, Mono<ServerResponse>> {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Override
    public Mono<ServerResponse> apply(ServerRequest request) {
        return request.bodyToMono(EmailRequest.class)
                .flatMap(emailRequest -> {
                    String email = emailRequest.getEmail();
                    return Mono.justOrEmpty(userService.findOrCreateUserByEmail(email))
                            .flatMap(user -> {
                                // 1. 응답 DTO 생성
                                UserResponse response = UserResponse.builder()
                                        .id(user.getId())
                                        .email(user.getEmail())
                                        .name(user.getName())
                                        .profileImage(user.getProfileImage())
                                        .provider(user.getProvider())
                                        .totalUseElecEstimate(user.getTotalUseElecEstimate())
                                        .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                                        .build();

                                // 2. JWT 토큰 생성 (비동기 처리)
                                return Mono.fromCallable(() ->
                                        jwtTokenService.generateToken(
                                                user.getId(),
                                                user.getEmail(),
                                                user.getRole().name()
                                        )
                                ).map(jwtToken -> Tuples.of(response, jwtToken));
                            });
                })
                .flatMap(tuple -> {
                    UserResponse response = tuple.getT1();
                    String jwtToken = tuple.getT2();

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .bodyValue(response);
                })
                .onErrorResume(JwtAuthenticationException.class, e ->
                        ServerResponse.status(401)
                                .bodyValue("Invalid JWT: " + e.getMessage())
                )
                .onErrorResume(Exception.class, e ->
                        ServerResponse.status(500)
                                .bodyValue("Internal error: " + e.getMessage())
                );
    }
}