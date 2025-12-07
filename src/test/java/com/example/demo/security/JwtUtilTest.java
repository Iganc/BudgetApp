package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Użyj surowego klucza (minimum 32 znaki dla HS256)
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "myVerySecureSecretKeyForJWT12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 86400000L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        String email = jwtUtil.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void extractUserId_ShouldReturnCorrectId() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        Long userId = jwtUtil.extractUserId(token);

        assertEquals(1L, userId);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        String token = jwtUtil.generateToken("test@example.com", 1L);

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForInvalidToken() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }
}