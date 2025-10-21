package com.bank.controller;

import com.bank.dto.AuthRequest;
import com.bank.dto.AuthResponse;
import com.bank.security.JwtTokenProvider;
import com.bank.service.AuditService;
import com.bank.service.MonitoringService;
import com.bank.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final MonitoringService monitoringService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest,
                                              HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String rateLimitKey = clientIp + "_/api/auth/login";

        // Check rate limiting
        if (rateLimitService.isRateLimited(rateLimitKey, RateLimitService.RateLimitType.LOGIN_ATTEMPT)) {
            auditService.logSecurityEvent("RATE_LIMIT_EXCEEDED",
                    "Login rate limit exceeded for IP: " + clientIp, false, request);
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }

        try {
            log.debug("Attempting authentication for user: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            // Record successful request
            rateLimitService.recordRequest(rateLimitKey, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

            // Логируем успешную аутентификацию
            auditService.logSecurityEvent("LOGIN_SUCCESS",
                    "User authenticated successfully", true, request);
            monitoringService.recordSuccessfulLogin(loginRequest.getUsername());

            log.info("User {} successfully authenticated", loginRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt, "Bearer"));

        } catch (BadCredentialsException e) {
            // Record failed request
            rateLimitService.recordRequest(rateLimitKey, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

            // Логируем неудачную попытку входа
            log.warn("Authentication failed for user: {} - Invalid credentials", loginRequest.getUsername());
            auditService.logSecurityEvent("LOGIN_FAILED",
                    "Invalid credentials", false, request);
            monitoringService.recordFailedLogin(loginRequest.getUsername());

            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            // Record failed request
            rateLimitService.recordRequest(rateLimitKey, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

            log.error("Unexpected error during authentication for user: {}", loginRequest.getUsername(), e);
            auditService.logSecurityEvent("LOGIN_ERROR",
                    "Unexpected error: " + e.getMessage(), false, request);
            return ResponseEntity.status(500).body("Authentication error");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String token,
                                           HttpServletRequest request) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                auditService.logSecurityEvent("TOKEN_VALIDATION_FAILED",
                        "Invalid token format", false, request);
                return ResponseEntity.badRequest().body("Invalid token");
            }

            String jwt = token.substring(7);
            if (tokenProvider.validateToken(jwt)) {
                auditService.logSecurityEvent("TOKEN_VALIDATION_SUCCESS",
                        "Token is valid", true, request);
                return ResponseEntity.ok().body("Token is valid");
            }

            auditService.logSecurityEvent("TOKEN_VALIDATION_FAILED",
                    "Invalid token", false, request);
            return ResponseEntity.badRequest().body("Invalid token");
        } catch (Exception e) {
            log.error("Error validating token", e);
            auditService.logSecurityEvent("TOKEN_VALIDATION_ERROR",
                    "Error validating token: " + e.getMessage(), false, request);
            return ResponseEntity.status(500).body("Token validation error");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
