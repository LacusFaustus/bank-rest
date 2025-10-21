package com.bank.controller;

import com.bank.config.TestSecurityConfig;
import com.bank.dto.AuthRequest;
import com.bank.security.JwtTokenProvider;
import com.bank.service.AuditService;
import com.bank.service.MonitoringService;
import com.bank.service.PasswordPolicyService;
import com.bank.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private MonitoringService monitoringService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest validAuthRequest;
    private AuthRequest invalidAuthRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup test data
        validAuthRequest = new AuthRequest();
        validAuthRequest.setUsername("testuser");
        validAuthRequest.setPassword("ValidPass123!");

        invalidAuthRequest = new AuthRequest();
        invalidAuthRequest.setUsername("testuser");
        invalidAuthRequest.setPassword("wrongpassword");

        // Setup mock authentication
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        // Setup audit service to do nothing
        doNothing().when(auditService).logSecurityEvent(anyString(), anyString(), anyBoolean(), any());

        // Setup rate limit service to allow requests
        when(rateLimitService.isRateLimited(anyString(), any())).thenReturn(false);
        doNothing().when(rateLimitService).recordRequest(anyString(), any());

        // Setup password policy service to allow valid passwords
        when(passwordPolicyService.validatePassword("ValidPass123!")).thenReturn(true);
        when(passwordPolicyService.validatePassword("wrongpassword")).thenReturn(true);
        when(passwordPolicyService.validatePassword("weak")).thenReturn(false);
    }

    @Test
    void authenticateUser_ValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("test-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(monitoringService).recordSuccessfulLogin("testuser");
        verify(auditService).logSecurityEvent(eq("LOGIN_SUCCESS"), anyString(), eq(true), any());
        verify(passwordPolicyService).validatePassword("ValidPass123!");
    }

    @Test
    void authenticateUser_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(monitoringService).recordFailedLogin("testuser");
        verify(auditService).logSecurityEvent(eq("LOGIN_FAILED"), anyString(), eq(false), any());
        verify(tokenProvider, never()).generateToken(any());
        verify(passwordPolicyService).validatePassword("wrongpassword");
    }

    @Test
    void authenticateUser_RateLimited_ShouldReturnTooManyRequests() throws Exception {
        // Given
        when(rateLimitService.isRateLimited(anyString(), any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Rate limit exceeded. Please try again later."));

        // Verify no authentication attempts when rate limited
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        verify(passwordPolicyService, never()).validatePassword(any());
    }

    @Test
    void validateToken_ValidToken_ShouldReturnOk() throws Exception {
        // Given
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));

        // Verify interactions
        verify(tokenProvider).validateToken("valid-token");
        verify(auditService).logSecurityEvent(eq("TOKEN_VALIDATION_SUCCESS"), anyString(), eq(true), any());
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnBadRequest() throws Exception {
        // Given
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));

        // Verify interactions
        verify(tokenProvider).validateToken("invalid-token");
        verify(auditService).logSecurityEvent(eq("TOKEN_VALIDATION_FAILED"), anyString(), eq(false), any());
    }

    @Test
    void validateToken_MissingToken_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));

        // Verify no token validation when token is missing
        verify(tokenProvider, never()).validateToken(any());
        verify(auditService).logSecurityEvent(eq("TOKEN_VALIDATION_FAILED"), anyString(), eq(false), any());
    }

    @Test
    void validateToken_MalformedToken_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));

        // Verify no token validation when token format is invalid
        verify(tokenProvider, never()).validateToken(any());
        verify(auditService).logSecurityEvent(eq("TOKEN_VALIDATION_FAILED"), anyString(), eq(false), any());
    }

    @Test
    void authenticateUser_EmptyUsername_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthRequest emptyUsernameRequest = new AuthRequest();
        emptyUsernameRequest.setUsername("");
        emptyUsernameRequest.setPassword("ValidPass123!");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUsernameRequest)))
                .andExpect(status().isBadRequest());

        // Verify no authentication attempts for invalid request
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        // Не проверяем вызовы PasswordPolicyService, так как Spring может не вызывать кастомные валидаторы при нарушении @NotBlank
    }

    @Test
    void authenticateUser_EmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthRequest emptyPasswordRequest = new AuthRequest();
        emptyPasswordRequest.setUsername("testuser");
        emptyPasswordRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                .andExpect(status().isBadRequest());

        // Verify no authentication attempts for invalid request
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        // Не проверяем вызовы PasswordPolicyService, так как Spring может не вызывать кастомные валидаторы при нарушении @NotBlank
    }

    @Test
    void authenticateUser_NullRequest_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // Verify no authentication attempts for invalid request
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        // Не проверяем вызовы PasswordPolicyService, так как Spring может не вызывать кастомные валидаторы при нарушении @NotBlank
    }

    @Test
    void authenticateUser_WeakPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthRequest weakPasswordRequest = new AuthRequest();
        weakPasswordRequest.setUsername("testuser");
        weakPasswordRequest.setPassword("weak");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPasswordRequest)))
                .andExpect(status().isBadRequest());

        // Verify no authentication attempts for weak password
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        // Для weak password проверяем вызов валидации, так как @NotBlank пройден
        verify(passwordPolicyService).validatePassword("weak");
    }

    @Test
    void authenticateUser_NullUsername_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthRequest nullUsernameRequest = new AuthRequest();
        nullUsernameRequest.setUsername(null);
        nullUsernameRequest.setPassword("ValidPass123!");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullUsernameRequest)))
                .andExpect(status().isBadRequest());

        // Verify no interactions for null username
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        // Не проверяем вызовы PasswordPolicyService, так как Spring может не вызывать кастомные валидаторы при нарушении @NotBlank
    }

    @Test
    void authenticateUser_NullPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthRequest nullPasswordRequest = new AuthRequest();
        nullPasswordRequest.setUsername("testuser");
        nullPasswordRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullPasswordRequest)))
                .andExpect(status().isBadRequest());

        // Verify no interactions for null password
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
        // Не проверяем вызовы PasswordPolicyService, так как Spring может не вызывать кастомные валидаторы при нарушении @NotBlank
    }
}
