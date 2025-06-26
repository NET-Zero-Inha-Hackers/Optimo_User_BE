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
                .andRoute(RequestPredicates.POST("/api/user"), this::handleJWTUser)
                .andRoute(RequestPredicates.PATCH("/api/elecAndCost"), this::handleElecAndCost);
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

    private Mono<ServerResponse> handleElecAndCost(ServerRequest request) {
        @SuppressWarnings("unchecked")
        Function<ServerRequest, Mono<ServerResponse>> func =
                (Function<ServerRequest, Mono<ServerResponse>>) functionCatalog.lookup("increaseElecAndCostFunction");
        if (func == null) {
            return ServerResponse.status(500).bodyValue("Function increaseElecAndCostFunction not found");
        }
        return func.apply(request);
    }
}