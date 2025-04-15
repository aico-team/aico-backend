package aico.backend.user.dto;

import aico.backend.global.annotation.EmailFormatValid;
import aico.backend.global.annotation.PasswordFormatValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequest {
    @EmailFormatValid
    @NotBlank(message = "사용자 이메일은 필수 항목입니다.")
    private String email;

    @PasswordFormatValid
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 항목입니다.")
    private String confirmPassword;

    @Size(max = 8)
    @NotBlank(message = "닉네임은 필수 항목입니다.")
    private String nickname;
}
