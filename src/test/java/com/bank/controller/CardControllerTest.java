package com.bank.controller;

import com.bank.config.RateLimitConfig;
import com.bank.config.TestSecurityConfig;
import com.bank.dto.TransferRequest;
import com.bank.service.CardService;
import com.bank.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CardController.class)
@ActiveProfiles("test")
@Import({RateLimitConfig.class, TestSecurityConfig.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void transferBetweenCards_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("100.00"));

        doNothing().when(cardService).transferBetweenCards(any(), eq(1L), eq(2L), any(BigDecimal.class));

        // When & Then
        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer completed successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void requestBlockCard_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(cardService).requestCardBlock(eq(1L), any());

        // When & Then
        mockMvc.perform(post("/api/cards/1/block-request"))
                .andExpect(status().isOk())
                .andExpect(content().string("Block request submitted for card: 1. Please contact administrator."));
    }
}
