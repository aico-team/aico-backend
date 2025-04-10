package aico.backend.user.service;

import aico.backend.global.exception.user.DuplicatedUserException;
import aico.backend.user.domain.Role;
import aico.backend.user.domain.User;
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
    public User signUp(String username, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicatedUserException("이미 가입된 이메일입니다.");
        }
        User user = User.createUser(username,
                        email,
                        passwordEncoder.encode(password),
                        role);
        userRepository.save(user);
        return user;
    }
}
