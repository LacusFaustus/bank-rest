package com.bank.validation;

import com.bank.service.PasswordPolicyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private final PasswordPolicyService passwordPolicyService;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return passwordPolicyService.validatePassword(password);
    }
}
