package org.inhahackers.optmo_user_be.config;

import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class FunctionRouterConfig {
    private final FunctionCatalog functionCatalog;

    public FunctionRouterConfig(FunctionCatalog functionCatalog) {
        this.functionCatalog = functionCatalog;
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route(RequestPredicates.POST("/api/oauthUser"), this::handleOAuthUser)
                .andRoute(RequestPredicates.POST("/api/jwtUser"), this::handleJWTUser)
                .andRoute(RequestPredicates.POST("/api/user"), this::handleUser)
                .andRoute(RequestPredicates.PATCH("/api/elec"), this::handleElec);
    }

    private Mono<ServerResponse> handleOAuthUser(ServerRequest request) {
        @SuppressWarnings("unchecked")
        Function<ServerRequest, Mono<ServerResponse>> func =
                (Function<ServerRequest, Mono<ServerResponse>>) functionCatalog.lookup("oAuthUserFunction");
        if (func == null) {
            return ServerResponse.status(500).bodyValue("Function oAuthUserFunction not found");
        }
        return func.apply(request);
    }

    private Mono<ServerResponse> handleJWTUser(ServerRequest request) {
        @SuppressWarnings("unchecked")
        Function<ServerRequest, Mono<ServerResponse>> func =
                (Function<ServerRequest, Mono<ServerResponse>>) functionCatalog.lookup("jwtUserFunction");
        if (func == null) {
            return ServerResponse.status(500).bodyValue("Function jwtUserFunction not found");
        }
        return func.apply(request);
    }

    private Mono<ServerResponse> handleUser(ServerRequest request) {
        @SuppressWarnings("unchecked")
        Function<ServerRequest, Mono<ServerResponse>> func =
                (Function<ServerRequest, Mono<ServerResponse>>) functionCatalog.lookup("userFunction");
        if (func == null) {
            return ServerResponse.status(500).bodyValue("Function userFunction not found");
        }
        return func.apply(request);
    }

    private Mono<ServerResponse> handleElec(ServerRequest request) {
        @SuppressWarnings("unchecked")
        Function<ServerRequest, Mono<ServerResponse>> func =
                (Function<ServerRequest, Mono<ServerResponse>>) functionCatalog.lookup("increaseElecFunction");
        if (func == null) {
            return ServerResponse.status(500).bodyValue("Function increaseElecFunction not found");
        }
        return func.apply(request);
    }
}