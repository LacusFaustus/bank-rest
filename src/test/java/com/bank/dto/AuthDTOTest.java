package com.bank.dto;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class AuthDTOTest {

    @Test
    void authRequest_ShouldWork() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("password");

        assertEquals("user", request.getUsername());
        assertEquals("password", request.getPassword());
    }

    @Test
    void authResponse_ShouldWork() {
        AuthResponse response = new AuthResponse("token", "Bearer");

        assertEquals("token", response.getToken());
        assertEquals("Bearer", response.getType());
    }
}
