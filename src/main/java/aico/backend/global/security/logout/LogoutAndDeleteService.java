package aico.backend.global.security.logout;

import aico.backend.global.security.JwtUtil;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LogoutAndDeleteService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public void logout(UserDetailsImpl userDetails) {
        String email = userDetails.getUsername();
        jwtUtil.destroyRefreshToken(email);
    }

    public void delete(UserDetailsImpl userDetails) {
        userRepository.findByEmail(userDetails.getUsername())
                .ifPresent(userRepository::delete);
    }
}
