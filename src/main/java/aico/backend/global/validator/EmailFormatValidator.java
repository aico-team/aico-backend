package aico.backend.global.validator;

import aico.backend.global.annotation.EmailFormatValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailFormatValidator implements ConstraintValidator<EmailFormatValid, String> {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return EMAIL_PATTERN.matcher(value).matches();
    }

}
