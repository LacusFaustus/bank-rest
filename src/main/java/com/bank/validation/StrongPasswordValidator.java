package com.bank.validation;

import com.bank.service.PasswordPolicyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private final PasswordPolicyService passwordPolicyService;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Spring Validation может вызвать этот метод с null или пустой строкой
        // даже при наличии @NotBlank, поэтому нужно корректно обработать эти случаи
        if (password == null || password.trim().isEmpty()) {
            // Возвращаем false для null и пустых строк, но не логируем ошибку
            // так как @NotBlank уже должен обработать эту ситуацию
            return false;
        }

        // Проверяем пароль через сервис политики паролей
        return passwordPolicyService.validatePassword(password);
    }

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // Инициализация не требуется
    }
}
