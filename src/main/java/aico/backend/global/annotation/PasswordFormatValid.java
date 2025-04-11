package aico.backend.global.annotation;

import aico.backend.global.validator.PasswordFormatValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordFormatValidator.class)
public @interface PasswordFormatValid {
    String message() default "비밀번호는 영문, 숫자, 특수문자만 4~15자 입력 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
