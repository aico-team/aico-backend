package aico.backend.user.service;

import aico.backend.global.exception.user.ConfirmPasswordMisException;
import aico.backend.global.exception.user.DuplicatedUserException;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.Role;
import aico.backend.user.domain.User;
import aico.backend.user.dto.SignUpRequest;
import aico.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (isDuplicatedEmail(request.getEmail())) {
            throw new DuplicatedUserException("이미 가입된 이메일입니다.");
        }
        if (isDuplicatedNickname(request.getNickname())) {
            throw new DuplicatedUserException("이미 존재하는 닉네임입니다.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ConfirmPasswordMisException("비밀번호와 비밀번호 확인값이 다릅니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.from(request,encodedPassword);
        userRepository.save(user);
    }

    public boolean isDuplicatedEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isDuplicatedNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public Optional<User> getCurrentUser(UserDetailsImpl userDetails) {
        Long id = userDetails.getUser().getId();
        return userRepository.findById(id);
    }
}
