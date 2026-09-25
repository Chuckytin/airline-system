package com.airline.security.jwt;

import com.airline.enums.UserRole;
import com.airline.model.User;
import com.airline.security.userdetails.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtServiceImpl Tests")
class JwtServiceImplTest {

    private static final String SECRET = "test-secret-key-for-testing-only-must-be-at-least-256-bits-long-please-change";
    private static final long EXPIRATION_MS = 3600000;

    private JwtServiceImpl jwtService;
    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(SECRET, EXPIRATION_MS);

        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedPassword")
                .fullName("John Doe")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();

        userDetails = new UserDetailsImpl(user);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should extract userId from token")
    void shouldExtractUserId() {
        String token = jwtService.generateToken(user);

        Long userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void shouldExtractExpiration() {
        String token = jwtService.generateToken(user);

        Instant expiration = jwtService.extractExpiration(token);

        assertThat(expiration).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateToken() {
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for token with different user")
    void shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(user);

        User anotherUser = User.builder()
                .id(2L)
                .email("jane@example.com")
                .password("encoded")
                .fullName("Jane")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();

        UserDetails anotherUserDetails = new UserDetailsImpl(anotherUser);

        boolean isValid = jwtService.isTokenValid(token, anotherUserDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void shouldReturnFalseForInvalidToken() {
        boolean isValid = jwtService.isTokenValid("invalid.token.here", userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return jwt expiration time")
    void shouldReturnExpirationTime() {
        long expiry = jwtService.getJwtExpiryMs();

        assertThat(expiry).isEqualTo(EXPIRATION_MS);
    }

    @Test
    @DisplayName("Should include role and userId claims in token")
    void shouldIncludeRoleClaim() {
        String token = jwtService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
        assertThat(claims.getSubject()).isEqualTo("john@example.com");
    }

}