package com.example.appupdater.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AppVersionValidator implements ConstraintValidator<ValidAppVersion, String> {

    private static final String VERSION_PATTERN = "^[0-9]+(\\.[0-9]+)*(-[a-zA-Z0-9]+)?$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return value.matches(VERSION_PATTERN);
    }
}