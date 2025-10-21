package com.bank.validation;

import com.bank.service.PasswordPolicyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StrongPasswordValidatorTest {

    @Mock
    private PasswordPolicyService passwordPolicyService;

    @Test
    void isValid_ValidPassword_ShouldReturnTrue() {
        // Given
        StrongPasswordValidator validator = new StrongPasswordValidator(passwordPolicyService);
        when(passwordPolicyService.validatePassword("ValidPass123!")).thenReturn(true);

        // When
        boolean result = validator.isValid("ValidPass123!", null);

        // Then
        assertTrue(result);
        verify(passwordPolicyService).validatePassword("ValidPass123!");
    }

    @Test
    void isValid_InvalidPassword_ShouldReturnFalse() {
        // Given
        StrongPasswordValidator validator = new StrongPasswordValidator(passwordPolicyService);
        when(passwordPolicyService.validatePassword("weak")).thenReturn(false);

        // When
        boolean result = validator.isValid("weak", null);

        // Then
        assertFalse(result);
        verify(passwordPolicyService).validatePassword("weak");
    }

    @Test
    void isValid_NullPassword_ShouldReturnFalse() {
        // Given
        StrongPasswordValidator validator = new StrongPasswordValidator(passwordPolicyService);

        // When
        boolean result = validator.isValid(null, null);

        // Then
        assertFalse(result);
        verify(passwordPolicyService, never()).validatePassword(any());
    }

    @Test
    void isValid_EmptyPassword_ShouldReturnFalse() {
        // Given
        StrongPasswordValidator validator = new StrongPasswordValidator(passwordPolicyService);

        // When
        boolean result = validator.isValid("", null);

        // Then
        assertFalse(result);
        verify(passwordPolicyService, never()).validatePassword(any());
    }

    @Test
    void isValid_BlankPassword_ShouldReturnFalse() {
        // Given
        StrongPasswordValidator validator = new StrongPasswordValidator(passwordPolicyService);

        // When
        boolean result = validator.isValid("   ", null);

        // Then
        assertFalse(result);
        verify(passwordPolicyService, never()).validatePassword(any());
    }
}
