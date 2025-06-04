package aico.backend.user.controller;

import aico.backend.global.security.JwtUtil;
import aico.backend.user.domain.User;
import aico.backend.user.dto.SignUpRequest;
import aico.backend.user.repository.UserRepository;
import aico.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        userService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/duplicate")
    public ResponseEntity<Boolean> duplicateCheck(@RequestParam String email) {
        Boolean isDuplicated = userService.isDuplicatedEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(isDuplicated);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.extractRefreshToken(request)
                .filter(jwtUtil::isTokenValid)
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing refresh token");
        }

        Optional<User> userOpt = userRepository.findByRefreshToken(refreshToken);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token not recognized");
        }

        User user = userOpt.get();

        String newAccessToken = jwtUtil.createAccessToken(user.getEmail(), user.getNickname());

        Map<String, String> tokenBody = new HashMap<>();
        tokenBody.put("accessToken", newAccessToken);

        return ResponseEntity.ok(tokenBody);
    }
}
