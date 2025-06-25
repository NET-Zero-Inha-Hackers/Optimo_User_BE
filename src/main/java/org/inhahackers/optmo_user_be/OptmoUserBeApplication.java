package org.inhahackers.optmo_user_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OptmoUserBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(OptmoUserBeApplication.class, args);
    }
}
