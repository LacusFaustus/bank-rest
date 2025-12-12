package com.bank.controller;

import com.bank.dto.AuthRequest;
import com.bank.dto.AuthResponse;
import com.bank.security.JwtTokenProvider;
import com.bank.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private AuditService auditService;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private AuthController authController;

    @Test
    void authenticateUser_ShouldReturnToken() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        Authentication authentication = mock(Authentication.class);

        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.isRateLimited(anyString(), any())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Когда токен генерируется, просто возвращаем строку
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        // When
        ResponseEntity<?> response = authController.authenticateUser(request, servletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("jwt-token", authResponse.getToken());

        // Проверяем, что сервисы были вызваны
        verify(monitoringService).recordSuccessfulLogin("user");
        verify(auditService).logSecurityEvent(eq("LOGIN_SUCCESS"), anyString(), eq(true), any());
    }

    @Test
    void authenticateUser_InvalidCredentials_ShouldReturn401() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("wrongpass");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.isRateLimited(anyString(), any())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        ResponseEntity<?> response = authController.authenticateUser(request, servletRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(monitoringService).recordFailedLogin("user");
    }

    @Test
    void authenticateUser_RateLimited_ShouldReturn429() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.isRateLimited(anyString(), any())).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.authenticateUser(request, servletRequest);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Rate limit exceeded. Please try again later.", response.getBody());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void validateToken_ValidToken_ShouldReturnOk() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "Bearer valid-token";

        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("valid-token")).thenReturn("testuser");

        // When
        ResponseEntity<?> response = authController.validateToken(token, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token is valid", response.getBody());
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnBadRequest() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "Bearer invalid-token";

        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // When
        ResponseEntity<?> response = authController.validateToken(token, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void validateToken_NullToken_ShouldReturnBadRequest() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);

        // When
        ResponseEntity<?> response = authController.validateToken(null, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void validateToken_InvalidFormat_ShouldReturnBadRequest() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "InvalidFormatToken";

        // When
        ResponseEntity<?> response = authController.validateToken(token, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void validateToken_Exception_ShouldReturn500() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "Bearer some-token";

        when(tokenProvider.validateToken("some-token")).thenThrow(new RuntimeException("Test exception"));

        // When
        ResponseEntity<?> response = authController.validateToken(token, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Token validation error", response.getBody());
    }
}
