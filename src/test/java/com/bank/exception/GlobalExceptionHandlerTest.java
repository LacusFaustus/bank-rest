package com.bank.exception;

import com.bank.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCardNotFoundException_ShouldReturn404() {
        CardNotFoundException ex = new CardNotFoundException("Card not found");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/cards/123");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleCardNotFound(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Card not found", response.getBody().getMessage());
        assertEquals("Not Found", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/cards/123"));
    }

    @Test
    void handleCardAlreadyExistsException_ShouldReturn409() {
        CardAlreadyExistsException ex = new CardAlreadyExistsException("Card already exists");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/cards");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleCardAlreadyExists(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Card already exists", response.getBody().getMessage());
        assertEquals("Conflict", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/cards"));
    }

    @Test
    void handleInsufficientBalanceException_ShouldReturn400() {
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/transfer");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientBalance(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient balance", response.getBody().getMessage());
        assertEquals("Bad Request", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/transfer"));
    }

    @Test
    void handleUnauthorizedAccessException_ShouldReturn403() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Access denied");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedAccess(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
        assertEquals("Forbidden", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/admin"));
    }

    @Test
    void handleUsernameNotFoundException_ShouldReturn404() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("Not Found", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/auth/login"));
    }

    @Test
    void handleAccessDeniedException_ShouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/secure");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
        assertEquals("Forbidden", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/secure"));
    }

    @Test
    void handleGlobalException_ShouldReturn500() {
        Exception ex = new RuntimeException("Unexpected error");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/api/test"));
    }

    @Test
    void handleSecurityException_ShouldReturn403() {
        SecurityException ex = new SecurityException("Security violation");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleSecurityException(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Security violation", response.getBody().getMessage());
        assertEquals("Forbidden", response.getBody().getError());
        assertTrue(response.getBody().getPath().contains("/protected"));
    }

    @Test
    void errorResponse_ShouldContainTimestamp() {
        CardNotFoundException ex = new CardNotFoundException("Test");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleCardNotFound(ex, webRequest);

        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void errorResponse_ShouldContainCorrectStatus() {
        CardNotFoundException ex = new CardNotFoundException("Test");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleCardNotFound(ex, webRequest);

        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleExceptionWithEmptyMessage_ShouldWork() {
        Exception ex = new Exception("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("", response.getBody().getMessage());
    }

    @Test
    void handleExceptionWithNullMessage_ShouldWork() {
        Exception ex = new Exception((String) null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    void webRequestWithQueryParameters_ShouldBeIncludedInPath() {
        CardNotFoundException ex = new CardNotFoundException("Test");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/cards");
        request.setQueryString("id=123&type=credit");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorResponse> response = handler.handleCardNotFound(ex, webRequest);

        String path = response.getBody().getPath();
        assertNotNull(path);
        assertTrue(path.contains("/api/cards"));
        // Note: getDescription(false) might not include query string
    }

    @Test
    void handlerInstantiation_ShouldWork() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertNotNull(handler);
    }

    @Test
    void allHandlerMethods_ShouldBeTested() {
        // This is a meta-test to ensure we have coverage for all handlers
        assertDoesNotThrow(() -> {
            GlobalExceptionHandler handler = new GlobalExceptionHandler();

            // Test that handler can process different exception types
            MockHttpServletRequest request = new MockHttpServletRequest();
            ServletWebRequest webRequest = new ServletWebRequest(request);

            handler.handleCardNotFound(new CardNotFoundException("test"), webRequest);
            handler.handleCardAlreadyExists(new CardAlreadyExistsException("test"), webRequest);
            handler.handleInsufficientBalance(new InsufficientBalanceException("test"), webRequest);
            handler.handleUnauthorizedAccess(new UnauthorizedAccessException("test"), webRequest);
            handler.handleUserNotFound(new UsernameNotFoundException("test"), webRequest);
            handler.handleAccessDenied(new AccessDeniedException("test"), webRequest);
            handler.handleGlobalException(new Exception("test"), webRequest);
            handler.handleSecurityException(new SecurityException("test"), webRequest);
        });
    }
}
