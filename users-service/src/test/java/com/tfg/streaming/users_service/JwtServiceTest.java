package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService — Tests Unitarios")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
    }

    @Test
    @DisplayName("generateToken → devuelve token no nulo y no vacío")
    void testGenerateToken_NotNull() {
        String token = jwtService.generateToken("user@vodeo.com", "USER", "vodeoUser");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("isTokenValid — token original → devuelve true")
    void testIsTokenValid_Success() {
        String token = jwtService.generateToken("user@vodeo.com", "USER", "vodeoUser");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("isTokenValid — token manipulado → devuelve false")
    void testIsTokenValid_TamperedToken_ReturnsFalse() {
        String token = jwtService.generateToken("user@vodeo.com", "USER", "vodeoUser");
        assertFalse(jwtService.isTokenValid(token + "tampered"));
    }

    @Test
    @DisplayName("extractEmail — extrae el subject correctamente")
    void testExtractEmail_CorrectSubject() {
        String token = jwtService.generateToken("user@vodeo.com", "USER", "vodeoUser");
        assertEquals("user@vodeo.com", jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("extractRole — extrae el rol correctamente")
    void testExtractRole_UserRole() {
        String token = jwtService.generateToken("admin@vodeo.com", "ADMIN", "adminUser");
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    @DisplayName("isTokenValid — token expirado → devuelve false")
    void testIsTokenValid_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1L);
        String expiredToken = jwtService.generateToken("user@vodeo.com", "USER", "vodeoUser");
        assertFalse(jwtService.isTokenValid(expiredToken));
    }
}