package com.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class PasswordPolicyService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public boolean validatePassword(String password) {
        if (password == null) {
            log.debug("Password is null - validation failed");
            return false;
        }

        // Для пустых строк сразу возвращаем false без подробного логирования
        if (password.isEmpty()) {
            log.debug("Password is empty - validation failed");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            log.debug("Password length invalid: {} (must be between {} and {})",
                    password.length(), MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
            return false;
        }

        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();

        log.debug("Password validation - Uppercase: {}, Lowercase: {}, Digit: {}, Special: {}",
                hasUppercase, hasLowercase, hasDigit, hasSpecialChar);

        if (!hasUppercase) {
            log.debug("Password must contain at least one uppercase letter");
            return false;
        }

        int additionalRequirementsMet = 0;
        if (hasLowercase) additionalRequirementsMet++;
        if (hasDigit) additionalRequirementsMet++;
        if (hasSpecialChar) additionalRequirementsMet++;

        // Требуем минимум одну дополнительную категорию (строчные буквы, цифры или специальные символы)
        boolean isValid = additionalRequirementsMet >= 1;

        if (!isValid) {
            log.debug("Password complexity requirements not met. Required: uppercase + 1 additional category, Actual additional: {}", additionalRequirementsMet);
        }

        return isValid;
    }

    public String generatePasswordRequirementsMessage() {
        return String.format(
                "Password must be between %d and %d characters long and contain: at least one uppercase letter, and at least one of the following: lowercase letters, numbers, special characters",
                MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH
        );
    }
}
