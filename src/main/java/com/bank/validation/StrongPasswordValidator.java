package com.bank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // Проверка минимальной длины
        if (password.length() < 8) {
            return false;
        }

        // Проверка наличия цифр
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Проверка наличия строчных букв
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Проверка наличия заглавных букв
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Проверка наличия специальных символов
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }

        return true;
    }
}
