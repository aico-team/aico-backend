package aico.backend.user.service;

import aico.backend.global.exception.user.ConfirmPasswordMisException;
import aico.backend.global.exception.user.DuplicatedUserException;
import aico.backend.user.domain.Role;
import aico.backend.user.domain.User;
import aico.backend.user.dto.SignUpRequest;
import aico.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicatedUserException("이미 가입된 이메일입니다.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ConfirmPasswordMisException("비밀번호와 비밀번호 확인값이 다릅니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.from(request,encodedPassword);
        userRepository.save(user);
    }
}
