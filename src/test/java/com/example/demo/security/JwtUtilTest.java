package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
            "thisIsAVeryLongSecretKeyForJwtTokenGenerationAndValidationInTestEnvironmentWithMoreThan256Bits");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 3600000L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void extractUserId_ShouldReturnCorrectId() {
        String token = jwtUtil.generateToken("test@example.com", 123L);

        Long userId = jwtUtil.extractUserId(token);

        assertThat(userId).isEqualTo(123L);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        boolean isValid = jwtUtil.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalid.token";

        boolean isValid = jwtUtil.isTokenValid(invalidToken);

        assertThat(isValid).isFalse();
    }
}