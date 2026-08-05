package com.bank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Password must be at least 8 characters long and contain at least one digit, one lowercase, one uppercase letter and one special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
