package org.inhahackers.optmo_user_be;

import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = JwtTokenService.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "jwt.secret=testtesttesttesttesttesttesttesttest",
                "jwt.access-token-validity-in-seconds=3600"
        }
)
class JwtTokenServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    @ParameterizedTest
    @CsvSource({
            "1, user1@example.com, ROLE_USER",
            "2, user2@example.com, ROLE_ADMIN",
            "3, user3@example.com, ROLE_DEMO"
    })
    void generateToken_and_extractData_success(Long userId, String email, String role) {
        // when
        String token = jwtTokenService.generateToken(userId, email, role);

        // then
        assertThat(jwtTokenService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenService.extractEmail(token)).isEqualTo(email);
        assertThat(jwtTokenService.extractRole(token)).isEqualTo(role);
    }

    @ParameterizedTest
    @CsvSource({
            "this.is.invalid.token",
            "123456",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload.signature"
    })
    void validateToken_withInvalidToken_shouldThrowException(String invalidToken) {
        assertThatThrownBy(() -> jwtTokenService.validateToken(invalidToken))
                .isInstanceOf(JwtAuthenticationException.class);
    }
}