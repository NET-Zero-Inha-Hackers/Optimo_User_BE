package org.inhahackers.optmo_user_be.config;

import org.inhahackers.optmo_user_be.function.JWTUserFunction;
import org.inhahackers.optmo_user_be.function.OAuthUserFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FunctionRouterConfig {
    private final OAuthUserFunction oAuthUserFunction;
    private final JWTUserFunction jwtUserFunction;

    public FunctionRouterConfig(OAuthUserFunction oAuthUserFunction, JWTUserFunction jwtUserFunction) {
        this.oAuthUserFunction = oAuthUserFunction;
        this.jwtUserFunction = jwtUserFunction;
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route(
                        RequestPredicates.POST("/api/oauthUser"),
                        oAuthUserFunction::apply
                ).andRoute(
                        RequestPredicates.POST("/api/user"),
                        jwtUserFunction::apply
                );
    }
}
