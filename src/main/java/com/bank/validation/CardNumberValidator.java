package com.bank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardNumberValidator implements ConstraintValidator<ValidCardNumber, String> {

    private boolean spaces;
    private boolean dashes;
    private boolean luhnCheck;

    @Override
    public void initialize(ValidCardNumber constraintAnnotation) {
        this.spaces = constraintAnnotation.spaces();
        this.dashes = constraintAnnotation.dashes();
        this.luhnCheck = constraintAnnotation.luhnCheck();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null считается валидным (обрабатывается другими аннотациями)
        if (value == null) {
            return true;
        }

        // Пустая строка или строка из пробелов - невалидна для номера карты
        if (value.trim().isEmpty()) {
            return false;
        }

        // Удаляем пробелы и тире если они разрешены
        String cleaned = value;
        if (spaces) {
            cleaned = cleaned.replaceAll("\\s+", "");
        }
        if (dashes) {
            cleaned = cleaned.replaceAll("-", "");
        }

        // Проверяем что все символы цифры
        if (!cleaned.matches("\\d+")) {
            return false;
        }

        // Проверяем длину (стандартные карты 13-19 цифр)
        if (cleaned.length() < 13 || cleaned.length() > 19) {
            return false;
        }

        // Проверка алгоритмом Луна если включена
        if (luhnCheck) {
            return isValidLuhn(cleaned);
        }

        return true;
    }

    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit % 10 + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    public int calculateLuhnCheckDigit(String number) {
        if (number == null || !number.matches("\\d+")) {
            return -1;
        }

        // Добавляем контрольную цифру 0
        String numberWithZero = number + "0";
        int sum = 0;
        boolean alternate = false;

        for (int i = numberWithZero.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(numberWithZero.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit % 10 + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }

    public boolean isTestCardNumber(String number) {
        if (number == null) return false;

        String cleaned = number.replaceAll("[\\s-]+", "");

        // Тестовые карты начинаются с определенных префиксов
        String[] testPrefixes = {"4", "5", "6", "34", "37"};

        for (String prefix : testPrefixes) {
            if (cleaned.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    public String detectCardType(String number) {
        if (number == null) {
            return "UNKNOWN";
        }

        String cleaned = number.replaceAll("[\\s-]+", "");

        if (cleaned.length() < 13 || cleaned.length() > 19) {
            return "UNKNOWN";
        }

        // Visa: начинается с 4
        if (cleaned.startsWith("4")) {
            return "VISA";
        }

        // MasterCard: начинается с 51-55
        if (cleaned.matches("^5[1-5].*")) {
            return "MASTERCARD";
        }

        // American Express: начинается с 34 или 37
        if (cleaned.startsWith("34") || cleaned.startsWith("37")) {
            return "AMEX";
        }

        // Discover: начинается с 6011, 65, или 644-649
        if (cleaned.startsWith("6011") || cleaned.startsWith("65") ||
                cleaned.matches("^64[4-9].*")) {
            return "DISCOVER";
        }

        return "UNKNOWN";
    }
}
