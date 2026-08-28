package com.finbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "finbank-test-secret-key-that-is-at-least-32-bytes-long",
                60_000
        );
    }

    @Test
    void generateToken_shouldContainNormalizedEmail() {
        String token = jwtService.generateToken(" ANIKET@EXAMPLE.COM ", "ROLE_CUSTOMER");

        assertNotNull(token);
        assertEquals("aniket@example.com", jwtService.extractEmail(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForMatchingUser() {
        String token = jwtService.generateToken("aniket@example.com", "ROLE_CUSTOMER");
        UserDetails user = User.withUsername("ANIKET@EXAMPLE.COM")
                .password("password")
                .roles("CUSTOMER")
                .build();

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken("aniket@example.com", "ROLE_CUSTOMER");
        UserDetails user = User.withUsername("other@example.com")
                .password("password")
                .roles("CUSTOMER")
                .build();

        assertFalse(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(
                "finbank-test-secret-key-that-is-at-least-32-bytes-long",
                1
        );

        String token = shortLived.generateToken("aniket@example.com", "ROLE_CUSTOMER");
        Thread.sleep(10);

        UserDetails user = User.withUsername("aniket@example.com")
                .password("password")
                .roles("CUSTOMER")
                .build();

        assertFalse(shortLived.isTokenValid(token, user));
    }

    @Test
    void extractEmail_shouldRejectTamperedToken() {
        String token = jwtService.generateToken("aniket@example.com", "ROLE_CUSTOMER");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(Exception.class, () -> jwtService.extractEmail(tampered));
    }
}
