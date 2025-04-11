package aico.backend.user.controller;

import aico.backend.user.dto.SignUpRequest;
import aico.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        userService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
