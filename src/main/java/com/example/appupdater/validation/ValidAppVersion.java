package com.example.appupdater.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AppVersionValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAppVersion {
    String message() default "Некорректный формат! Версия должна содержать только цифры, точки и тире (например: 1.0.0 или 2.1-beta)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}