package aico.backend.global.validator;


import aico.backend.global.annotation.PasswordFormatValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordFormatValidator implements ConstraintValidator<PasswordFormatValid, String> {
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9!@#$%^&*()_+=-]{4,15}";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return PASSWORD_PATTERN.matcher(value).matches();
    }
}
