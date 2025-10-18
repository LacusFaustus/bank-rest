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

    // Убираем конструктор по умолчанию, используем только Spring-инъекцию
    public PasswordPolicyService() {
        // Пустой конструктор для Spring
    }

    public boolean validatePassword(String password) {
        if (password == null) {
            log.warn("Password is null");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            log.warn("Password length invalid: {} (must be between {} and {})",
                    password.length(), MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
            return false;
        }

        // Обязательные требования - СТРОГАЯ ВАЛИДАЦИЯ
        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();

        log.debug("Password validation - Uppercase: {}, Lowercase: {}, Digit: {}, Special: {}",
                hasUppercase, hasLowercase, hasDigit, hasSpecialChar);

        // СТРОГИЕ ТРЕБОВАНИЯ: обязательно верхний регистр + минимум 2 другие категории
        if (!hasUppercase) {
            log.warn("Password must contain at least one uppercase letter");
            return false;
        }

        int additionalRequirementsMet = 0;
        if (hasLowercase) additionalRequirementsMet++;
        if (hasDigit) additionalRequirementsMet++;
        if (hasSpecialChar) additionalRequirementsMet++;

        // Требуем как минимум 2 дополнительные категории
        boolean isValid = additionalRequirementsMet >= 2;

        if (!isValid) {
            log.warn("Password complexity requirements not met. Required: uppercase + 2 additional categories, Actual additional: {}", additionalRequirementsMet);
        }

        return isValid;
    }

    public String generatePasswordRequirementsMessage() {
        return String.format(
                "Password must be between %d and %d characters long and contain: at least one uppercase letter, and at least two of the following: lowercase letters, numbers, special characters",
                MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH
        );
    }
}
