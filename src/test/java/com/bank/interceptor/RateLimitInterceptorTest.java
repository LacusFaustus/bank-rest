package com.bank.interceptor;

import com.bank.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private RateLimitInterceptor interceptor;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor(rateLimitService);
        // Не создаем responseWriter здесь, т.к. он нужен не во всех тестах
    }

    @Test
    void preHandle_WhenRateLimited_ShouldReturnFalse() throws Exception {
        // Arrange
        String clientIp = "192.168.1.1";
        String endpoint = "/auth/login";
        StringWriter responseWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        String rateLimitKey = clientIp + "_" + endpoint;
        when(rateLimitService.isRateLimited(eq(rateLimitKey), any(RateLimitService.RateLimitType.class)))
                .thenReturn(true);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertFalse(result);
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
    }

    @Test
    void preHandle_WhenNotRateLimited_ShouldReturnTrue() throws Exception {
        // Arrange
        String clientIp = "192.168.1.1";
        String endpoint = "/api/cards";

        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(clientIp);

        String rateLimitKey = clientIp + "_" + endpoint;
        when(rateLimitService.isRateLimited(eq(rateLimitKey), any(RateLimitService.RateLimitType.class)))
                .thenReturn(false);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(rateLimitService).recordRequest(eq(rateLimitKey), any(RateLimitService.RateLimitType.class));
    }

    @Test
    void preHandle_WithXForwardedForHeader_ShouldUseFirstIp() throws Exception {
        // Arrange
        String xForwardedFor = "192.168.1.1, 10.0.0.1";
        String endpoint = "/transfer";

        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor);

        String rateLimitKey = "192.168.1.1_" + endpoint;
        when(rateLimitService.isRateLimited(eq(rateLimitKey), any(RateLimitService.RateLimitType.class)))
                .thenReturn(false);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(rateLimitService).recordRequest(eq(rateLimitKey), any(RateLimitService.RateLimitType.class));
    }

    @ParameterizedTest
    @CsvSource({
            "/auth/login, LOGIN_ATTEMPT",
            "/api/auth/login, LOGIN_ATTEMPT",
            "/transfer, TRANSFER_OPERATION",
            "/api/transfer, TRANSFER_OPERATION",
            "/api/cards, API_REQUEST",
            "/api/users, API_REQUEST",
            "/other/endpoint, API_REQUEST"
    })
    void testRateLimitTypeMapping(String endpoint, RateLimitService.RateLimitType expectedType) throws Exception {
        // Arrange
        String clientIp = "192.168.1.1";

        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(clientIp);

        String rateLimitKey = clientIp + "_" + endpoint;
        when(rateLimitService.isRateLimited(eq(rateLimitKey), eq(expectedType)))
                .thenReturn(false);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        verify(rateLimitService).recordRequest(eq(rateLimitKey), eq(expectedType));
    }

    @Test
    void preHandle_WhenIOExceptionOccurs_ShouldLogError() throws Exception {
        // Arrange
        String clientIp = "192.168.1.1";
        String endpoint = "/auth/login";

        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(clientIp);

        when(rateLimitService.isRateLimited(anyString(), any(RateLimitService.RateLimitType.class)))
                .thenReturn(true);

        // Мокируем выброс исключения при получении writer
        when(response.getWriter()).thenThrow(new IOException("IO Error"));

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert - проверяем что метод обработал исключение и вернул false
        assertFalse(result);
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        // Не проверяем запись в writer, т.к. она бросила исключение
    }

    @Test
    void preHandle_WhenNullEndpoint_ShouldHandleGracefully() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        when(rateLimitService.isRateLimited(anyString(), any(RateLimitService.RateLimitType.class)))
                .thenReturn(false);

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
    }
}
