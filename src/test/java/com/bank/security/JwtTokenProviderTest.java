package com.bank.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Создаем валидный 256-битный ключ в Base64
        String secretKey = Base64.getEncoder().encodeToString(
                "12345678901234567890123456789012".getBytes() // 32 байта = 256 бит
        );
        jwtTokenProvider = new JwtTokenProvider(secretKey, 3600000L); // 1 час
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act
        String token = jwtTokenProvider.generateToken(auth);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.generateToken(auth);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
        assertFalse(jwtTokenProvider.validateToken(null));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken("   "));
    }

    @Test
    void getUsernameFromToken_withValidToken_shouldReturnUsername() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "john.doe",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.generateToken(auth);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("john.doe", username);
    }

    @Test
    void getUsernameFromToken_withInvalidToken_shouldReturnNull() {
        // Act
        String username = jwtTokenProvider.getUsernameFromToken("invalid.token");

        // Assert
        assertNull(username);
    }

    @Test
    void getClaimsFromToken_withValidToken_shouldReturnClaims() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.generateToken(auth);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("auth", String.class));
    }

    @Test
    void getClaimsFromToken_withInvalidToken_shouldReturnNull() {
        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken("invalid");

        // Assert
        assertNull(claims);
    }

    @Test
    void generateToken_withMultipleRoles_shouldIncludeAll() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                )
        );

        // Act
        String token = jwtTokenProvider.generateToken(auth);

        // Assert
        assertNotNull(token);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        String authClaim = claims.get("auth", String.class);
        assertTrue(authClaim.contains("ROLE_ADMIN"));
        assertTrue(authClaim.contains("ROLE_USER"));
    }

    @Test
    void getUsernameFromJWT_shouldWork() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.generateToken(auth);

        // Act
        String username = jwtTokenProvider.getUsernameFromJWT(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void constructor_withValidKey_shouldWork() {
        // Arrange
        String validKey = Base64.getEncoder().encodeToString(
                "another-32-byte-key-for-testing-here".getBytes()
        );

        // Act
        JwtTokenProvider provider = new JwtTokenProvider(validKey, 3600000L);

        // Assert
        assertNotNull(provider);
    }
}
