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
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    public boolean validatePassword(String password) {
        if (password == null) {
            log.debug("Password is null - validation failed");
            return false;
        }

        if (password.isEmpty()) {
            log.debug("Password is empty - validation failed");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            log.debug("Password length invalid: {} (must be between {} and {})",
                    password.length(), MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
            return false;
        }

        // Проверяем на наличие пробелов
        if (WHITESPACE_PATTERN.matcher(password).find()) {
            log.debug("Password contains whitespace - validation failed");
            return false;
        }

        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();

        log.debug("Password validation - Uppercase: {}, Lowercase: {}, Digit: {}, Special: {}",
                hasUppercase, hasLowercase, hasDigit, hasSpecialChar);

        // Обязательные требования:
        // 1. Должна быть хотя бы одна заглавная буква
        // 2. Должна быть хотя бы одна строчная буква
        // 3. Должна быть хотя бы одна цифра ИЛИ хотя бы один специальный символ
        if (!hasUppercase) {
            log.debug("Password must contain at least one uppercase letter");
            return false;
        }

        if (!hasLowercase) {
            log.debug("Password must contain at least one lowercase letter");
            return false;
        }

        // Требуется хотя бы одно из: цифры ИЛИ специальные символы
        boolean hasDigitOrSpecial = hasDigit || hasSpecialChar;
        if (!hasDigitOrSpecial) {
            log.debug("Password must contain at least one digit or special character");
            return false;
        }

        return true;
    }

    public String generatePasswordRequirementsMessage() {
        return String.format(
                "Password must be between %d and %d characters long, contain no whitespace, and contain: at least one uppercase letter, at least one lowercase letter, and at least one digit or special character",
                MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH
        );
    }
}
