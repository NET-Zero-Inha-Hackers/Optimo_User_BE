package org.inhahackers.optmo_user_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class AzureFunctionConfig {

    @Bean
    public Function<Map<String, Object>, Map<String, Object>> oAuthUserFunction() {
        return input -> {
            Map<String, Object> response = new HashMap<>();
            response.put("function", "oAuthUserFunction");
            response.put("received", input);
            return response;
        };
    }

    @Bean
    public Function<Map<String, Object>, Map<String, Object>> jwtUserFunction() {
        return input -> {
            Map<String, Object> response = new HashMap<>();
            response.put("function", "jwtUserFunction");
            response.put("received", input);
            return response;
        };
    }

    @Bean
    public Function<Map<String, Object>, Map<String, Object>> userFunction() {
        return input -> {
            Map<String, Object> response = new HashMap<>();
            response.put("function", "userFunction");
            response.put("received", input);
            return response;
        };
    }

    @Bean
    public Function<Map<String, Object>, Map<String, Object>> increaseElecFunction() {
        return input -> {
            Map<String, Object> response = new HashMap<>();
            response.put("function", "increaseElecFunction");
            response.put("received", input);
            return response;
        };
    }
}