package org.inhahackers.optmo_user_be.function;

import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.ElecRequest;
import org.inhahackers.optmo_user_be.exception.InvalidAuthorizationHeaderException;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.inhahackers.optmo_user_be.util.AuthorizationHeaderUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class IncreaseElecFunction implements Function<ServerRequest, Mono<ServerResponse>> {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Override
    public Mono<ServerResponse> apply(ServerRequest request) {
        // 1. Authorization 헤더 추출
        String accessToken;
        try {
            accessToken = AuthorizationHeaderUtil.extractToken(request.headers());
        } catch (InvalidAuthorizationHeaderException e) {
            return ServerResponse.badRequest().bodyValue(e.getMessage());
        }

        // 2. Request Body로 데이터 받기
        Mono<ElecRequest> elecAndCostRequestMono = request.bodyToMono(ElecRequest.class);

        return elecAndCostRequestMono.flatMap(elecRequest -> {
            try {
                // 3. 검증 및 정보 추출
                Long userId = jwtTokenService.extractUserId(accessToken);

                userService.increaseElecEstimate(userId, elecRequest);

                // 4. 응답
                return ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue("Successfully Increase Elec and Cost Estimate");
            } catch (JwtAuthenticationException e) {
                return ServerResponse.status(401)
                        .bodyValue("Invalid JWT: " + e.getMessage());
            } catch (UserNotFoundException e) {
                return ServerResponse.status(404)
                        .bodyValue("User Not Found: " + e.getMessage());
            } catch (Exception e) {
                return ServerResponse.status(500)
                        .bodyValue("Internal error: " + e.getMessage());
            }
        });
    }
}