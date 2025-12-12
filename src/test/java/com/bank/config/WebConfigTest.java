package com.bank.config;

import com.bank.interceptor.RateLimitInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private RateLimitInterceptor rateLimitInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig(rateLimitInterceptor);
    }

    @Test
    void addInterceptors_whenRateLimitEnabled_shouldAddInterceptor() {
        // Arrange
        ReflectionTestUtils.setField(webConfig, "rateLimitEnabled", true);

        when(interceptorRegistry.addInterceptor(any(RateLimitInterceptor.class)))
                .thenReturn(interceptorRegistration);

        when(interceptorRegistration.addPathPatterns(any(String[].class)))
                .thenReturn(interceptorRegistration);

        when(interceptorRegistration.excludePathPatterns(any(String[].class)))
                .thenReturn(interceptorRegistration);

        // Act
        webConfig.addInterceptors(interceptorRegistry);

        // Assert
        verify(interceptorRegistry).addInterceptor(rateLimitInterceptor);
        verify(interceptorRegistration).addPathPatterns("/**");
        verify(interceptorRegistration).excludePathPatterns("/h2-console/**", "/error", "/actuator/**");
    }

    @Test
    void addInterceptors_whenRateLimitDisabled_shouldNotAddInterceptor() {
        // Arrange
        ReflectionTestUtils.setField(webConfig, "rateLimitEnabled", false);

        // Act
        webConfig.addInterceptors(interceptorRegistry);

        // Assert
        verify(interceptorRegistry, never()).addInterceptor(any());
        verifyNoInteractions(interceptorRegistration);
    }

    @Test
    void constructor_shouldInitializeDependencies() {
        // Act & Assert
        assertNotNull(ReflectionTestUtils.getField(webConfig, "rateLimitInterceptor"));
        assertEquals(rateLimitInterceptor, ReflectionTestUtils.getField(webConfig, "rateLimitInterceptor"));
    }
}
