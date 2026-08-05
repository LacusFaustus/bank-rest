package com.bank.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Test
    void jwtAuthenticationFilter_shouldReturnFilter() {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig(unauthorizedHandler, tokenProvider, userDetailsService);

        // Act
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter();

        // Assert
        assertNotNull(filter);
    }

    @Test
    void authenticationProvider_shouldReturnConfiguredProvider() {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig(unauthorizedHandler, tokenProvider, userDetailsService);

        // Act
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider();

        // Assert
        assertNotNull(provider);
    }

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig(unauthorizedHandler, tokenProvider, userDetailsService);

        // Act
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);

        // Простая проверка работы энкодера
        String password = "test123";
        String encoded = encoder.encode(password);
        assertNotNull(encoded);
        assertNotEquals(password, encoded);
    }

    @Test
    void corsConfigurationSource_shouldReturnNonNull() {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig(unauthorizedHandler, tokenProvider, userDetailsService);

        // Act
        var corsSource = securityConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsSource);
    }

    @Test
    void authenticationManager_shouldReturnManager() throws Exception {
        // Arrange
        SecurityConfig securityConfig = new SecurityConfig(unauthorizedHandler, tokenProvider, userDetailsService);
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(expectedManager);

        // Act
        AuthenticationManager manager = securityConfig.authenticationManager(authConfig);

        // Assert
        assertNotNull(manager);
        assertEquals(expectedManager, manager);
    }
}
