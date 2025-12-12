package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceTest {

    private PasswordPolicyService passwordPolicyService;

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService();
    }

    @Test
    void validatePassword_StrongPassword_ShouldReturnTrue() {
        String strongPassword = "StrongPass123!";
        boolean isValid = passwordPolicyService.validatePassword(strongPassword);
        assertTrue(isValid, "Strong password with all categories should be valid");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseDigits_ShouldReturnTrue() {
        String password = "Password123";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase, lowercase and digits should be valid");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseSpecial_ShouldReturnTrue() {
        String password = "Password!@#";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase, lowercase and special characters should be valid");
    }

    @Test
    void validatePassword_UpperCaseDigitsSpecial_ShouldReturnFalse() {
        // Без строчных букв - должно быть false
        String password = "PASSWORD123!";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertFalse(isValid, "Password without lowercase letters should be invalid");
    }

    @Test
    void validatePassword_OnlyUppercaseAndDigits_ShouldReturnFalse() {
        // Без строчных букв - должно быть false
        String password = "PASSWORD123";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertFalse(isValid, "Password without lowercase letters should be invalid");
    }

    @Test
    void validatePassword_OnlyUppercaseAndSpecial_ShouldReturnFalse() {
        // Без строчных букв - должно быть false
        String password = "PASSWORD!@#";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertFalse(isValid, "Password without lowercase letters should be invalid");
    }

    @Test
    void validatePassword_OnlyUppercaseAndLowercase_ShouldReturnFalse() {
        // Без цифр и без специальных символов - должно быть false
        String password = "Password";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertFalse(isValid, "Password without digits or special characters should be invalid");
    }

    @Test
    void validatePassword_TooShort_ShouldReturnFalse() {
        String shortPassword = "Short1!";
        boolean isValid = passwordPolicyService.validatePassword(shortPassword);
        assertFalse(isValid, "Password shorter than 8 characters should be invalid");
    }

    @Test
    void validatePassword_TooLong_ShouldReturnFalse() {
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 129; i++) {
            longPassword.append("A");
        }
        longPassword.append("a1!");

        boolean isValid = passwordPolicyService.validatePassword(longPassword.toString());
        assertFalse(isValid, "Password longer than 128 characters should be invalid");
    }

    @Test
    void validatePassword_NullPassword_ShouldReturnFalse() {
        boolean isValid = passwordPolicyService.validatePassword(null);
        assertFalse(isValid, "Null password should be invalid");
    }

    @Test
    void validatePassword_EmptyPassword_ShouldReturnFalse() {
        boolean isValid = passwordPolicyService.validatePassword("");
        assertFalse(isValid, "Empty password should be invalid");
    }

    @Test
    void validatePassword_NoUppercase_ShouldReturnFalse() {
        String noUppercase = "lowercase123!";
        boolean isValid = passwordPolicyService.validatePassword(noUppercase);
        assertFalse(isValid, "Password without uppercase should be invalid");
    }

    @Test
    void validatePassword_OnlyUppercase_ShouldReturnFalse() {
        String onlyUppercase = "PASSWORD";
        boolean isValid = passwordPolicyService.validatePassword(onlyUppercase);
        assertFalse(isValid, "Password with only uppercase should be invalid");
    }

    @Test
    void validatePassword_OnlyLowercase_ShouldReturnFalse() {
        String onlyLowercase = "password";
        boolean isValid = passwordPolicyService.validatePassword(onlyLowercase);
        assertFalse(isValid, "Password with only lowercase should be invalid");
    }

    @Test
    void validatePassword_OnlyDigits_ShouldReturnFalse() {
        String onlyDigits = "12345678";
        boolean isValid = passwordPolicyService.validatePassword(onlyDigits);
        assertFalse(isValid, "Password with only digits should be invalid");
    }

    @Test
    void validatePassword_OnlySpecial_ShouldReturnFalse() {
        String onlySpecial = "!@#$%^&*";
        boolean isValid = passwordPolicyService.validatePassword(onlySpecial);
        assertFalse(isValid, "Password with only special characters should be invalid");
    }

    // Параметризованные тесты
    @ParameterizedTest
    @ValueSource(strings = {
            "ValidPass1",        // Верхний + нижний + цифры
            "ValidPassword123",  // Длиннее 8
            "Password!",         // Верхний + нижний + спецсимвол
            "Pass123!",          // Верхний + нижний + цифры + спецсимвол
            "Pass!@#$%"          // Верхний + нижний + спецсимволы
    })
    void validatePassword_ValidPasswords_ShouldReturnTrue(String password) {
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password '" + password + "' should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1",            // Слишком короткий
            "nouppercase123!",   // Нет верхнего регистра
            "NOLOWERCASE123!",   // Нет нижнего регистра (теперь должно быть false)
            "OnlyUPPERCASE",     // Только верхний регистр (теперь должно быть false)
            "onlylowercase",     // Только нижний регистр
            "12345678",          // Только цифры
            "!@#$%^&*",          // Только спецсимволы
            "Aa",                // Слишком короткий
            "Aa1!Bb2",           // 7 символов
            "Password",          // Нет цифр или спецсимволов (теперь должно быть false)
            "PASSWORD123",       // Нет строчных букв (теперь должно быть false)
            "password123!"       // Нет заглавных букв
    })
    void validatePassword_InvalidPasswords_ShouldReturnFalse(String password) {
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertFalse(isValid, "Password '" + password + "' should be invalid");
    }

    @Test
    void validatePassword_BoundaryTests() {
        // Граничные случаи
        assertFalse(passwordPolicyService.validatePassword("")); // Пустая строка
        assertFalse(passwordPolicyService.validatePassword("       ")); // Только пробелы
        assertFalse(passwordPolicyService.validatePassword("Aa1!")); // 4 символа
        assertFalse(passwordPolicyService.validatePassword("Aa1!Bb2")); // 7 символов
        assertTrue(passwordPolicyService.validatePassword("Aa1!Bb2C")); // 8 символов (верхний + нижний + цифра/спец)
        assertTrue(passwordPolicyService.validatePassword("Aa1!Bb2Cc")); // 9 символов

        // Проверка максимальной длины (128 символов)
        StringBuilder maxLengthPassword = new StringBuilder();
        for (int i = 0; i < 124; i++) { // 124 заглавных
            maxLengthPassword.append("A");
        }
        maxLengthPassword.append("a1!"); // + строчная + цифра + спецсимвол = 127 символов (124 + 3)

        // Исправлено: 124 + 3 = 127, а не 128
        assertEquals(127, maxLengthPassword.length());
        assertTrue(passwordPolicyService.validatePassword(maxLengthPassword.toString()));

        // Проверка точно 128 символов
        StringBuilder exact128Password = new StringBuilder();
        for (int i = 0; i < 125; i++) { // 125 заглавных
            exact128Password.append("A");
        }
        exact128Password.append("a1!"); // + строчная + цифра + спецсимвол = 128 символов (125 + 3)

        assertEquals(128, exact128Password.length());
        assertTrue(passwordPolicyService.validatePassword(exact128Password.toString()));

        // Слишком длинный пароль (129 символов)
        StringBuilder tooLongPassword = new StringBuilder();
        for (int i = 0; i < 126; i++) { // 126 заглавных
            tooLongPassword.append("A");
        }
        tooLongPassword.append("a1!"); // + строчная + цифра + спецсимвол = 129 символов (126 + 3)

        assertEquals(129, tooLongPassword.length());
        assertFalse(passwordPolicyService.validatePassword(tooLongPassword.toString()));
    }

    @Test
    void validatePassword_WhitespaceInPassword() {
        // Пароли с пробелами
        assertFalse(passwordPolicyService.validatePassword("Password 1!"));
        assertFalse(passwordPolicyService.validatePassword(" Password1!"));
        assertFalse(passwordPolicyService.validatePassword("Password1! "));
        assertFalse(passwordPolicyService.validatePassword("Pass word1!"));
        assertFalse(passwordPolicyService.validatePassword("Pass\tword1!"));
        assertFalse(passwordPolicyService.validatePassword("Pass\nword1!"));
    }

    @Test
    void generatePasswordRequirementsMessage_ShouldContainAllRequirements() {
        String message = passwordPolicyService.generatePasswordRequirementsMessage();

        assertNotNull(message);
        assertTrue(message.contains("8"), "Should mention minimum length 8");
        assertTrue(message.contains("128"), "Should mention maximum length 128");
        assertTrue(message.contains("uppercase"), "Should mention uppercase requirement");
        assertTrue(message.contains("lowercase"), "Should mention lowercase requirement");
        assertTrue(message.contains("digit or special character"), "Should mention digit or special character requirement");
        assertTrue(message.contains("no whitespace"), "Should mention no whitespace requirement");
    }

    @Test
    void validatePassword_BoundaryLength128WithWhitespace_ShouldReturnFalse() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 127; i++) {
            password.append("A");
        }
        password.append(" "); // добавляем пробел в конце

        assertFalse(passwordPolicyService.validatePassword(password.toString()));
    }

    @Test
    void validatePassword_OnlyDigitsAndSpecial_ShouldReturnFalse() {
        assertFalse(passwordPolicyService.validatePassword("1234!@#$"));
    }

    @Test
    void validatePassword_PasswordWithNewline_ShouldReturnFalse() {
        assertFalse(passwordPolicyService.validatePassword("Pass1!\nPass2!"));
        assertFalse(passwordPolicyService.validatePassword("\nPass1!"));
        assertFalse(passwordPolicyService.validatePassword("Pass1!\n"));
    }

    @Test
    void validatePassword_PasswordWithTab_ShouldReturnFalse() {
        assertFalse(passwordPolicyService.validatePassword("Pass1!\tPass2!"));
        assertFalse(passwordPolicyService.validatePassword("\tPass1!"));
        assertFalse(passwordPolicyService.validatePassword("Pass1!\t"));
    }

    @Test
    void validatePassword_ExactMinLength_ShouldReturnTrue() {
        // Точная минимальная длина с разными комбинациями
        assertTrue(passwordPolicyService.validatePassword("Aa1!Bb2C")); // 8 символов
        assertTrue(passwordPolicyService.validatePassword("A1!b2C3d")); // 8 символов
        assertTrue(passwordPolicyService.validatePassword("A!b#c$d%")); // 8 символов, только буквы и спецсимволы
    }

    @Test
    void validatePassword_ExactMaxLength_ShouldReturnTrue() {
        // Создаем пароль длиной 128 символов
        StringBuilder password = new StringBuilder();
        // 124 заглавных буквы + "a1!" (строчная + цифра + спецсимвол) = 127
        for (int i = 0; i < 124; i++) {
            password.append("A");
        }
        password.append("a1!");

        // Добавляем еще одну заглавную, чтобы получить 128
        password.append("A");

        assertEquals(128, password.length());
        assertTrue(passwordPolicyService.validatePassword(password.toString()));
    }

    @Test
    void validatePassword_129Characters_ShouldReturnFalse() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 126; i++) { // 126 заглавных
            password.append("A");
        }
        password.append("a1!A"); // + строчная + цифра + спецсимвол + заглавная = 130? Проверим

        // Давайте точно создадим 129 символов
        password = new StringBuilder();
        // 125 заглавных + "a1!A" (4 символа) = 129
        for (int i = 0; i < 125; i++) {
            password.append("A");
        }
        password.append("a1!A");

        assertEquals(129, password.length());
        assertFalse(passwordPolicyService.validatePassword(password.toString()));
    }

    @Test
    void validatePassword_AllSpecialCharacters_ShouldReturnFalseIfNoLetters() {
        // Только спецсимволы - должно быть false (нет букв)
        assertFalse(passwordPolicyService.validatePassword("!@#$%^&*"));

        // Спецсимволы + цифры - должно быть false (нет букв)
        assertFalse(passwordPolicyService.validatePassword("!@#$1234"));
    }

    @Test
    void validatePassword_MixedCaseWithSpecial_EdgeCases() {
        // Граничные случаи с разными комбинациями
        assertTrue(passwordPolicyService.validatePassword("A" + "a".repeat(6) + "1")); // Aaaaaaa1
        assertTrue(passwordPolicyService.validatePassword("A" + "a".repeat(6) + "!"));

        // Только одна строчная, но есть спецсимвол
        assertTrue(passwordPolicyService.validatePassword("AAAAAAAa!"));

        // Только одна заглавная, но есть цифра
        assertTrue(passwordPolicyService.validatePassword("Aaaaaaaa1"));
    }

    @Test
    void generatePasswordRequirementsMessage_ShouldBeConsistent() {
        String message1 = passwordPolicyService.generatePasswordRequirementsMessage();
        String message2 = passwordPolicyService.generatePasswordRequirementsMessage();

        assertEquals(message1, message2);
        assertNotNull(message1);
        assertFalse(message1.isEmpty());

        // Проверим конкретный формат
        assertTrue(message1.contains("between 8 and 128"));
        assertTrue(message1.contains("no whitespace"));
        assertTrue(message1.contains("uppercase"));
        assertTrue(message1.contains("lowercase"));
        assertTrue(message1.contains("digit or special character"));
    }
}
