package com.bank.controller;

import com.bank.dto.AuthRequest;
import com.bank.dto.AuthResponse;
import com.bank.security.JwtTokenProvider;
import com.bank.service.AuditService;
import com.bank.service.MonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final MonitoringService monitoringService;
    private final AuditService auditService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest,
                                              HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            // Логируем успешную аутентификацию
            auditService.logSecurityEvent("LOGIN_SUCCESS",
                    "User authenticated successfully", true, request);
            monitoringService.recordSuccessfulLogin(loginRequest.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt, "Bearer"));
        } catch (BadCredentialsException e) {
            // Логируем неудачную попытку входа
            auditService.logSecurityEvent("LOGIN_FAILED",
                    "Invalid credentials", false, request);
            monitoringService.recordFailedLogin(loginRequest.getUsername());

            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String token,
                                           HttpServletRequest request) {
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
    }
}
