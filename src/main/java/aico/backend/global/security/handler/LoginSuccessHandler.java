package aico.backend.global.security.handler;

import aico.backend.global.security.JwtUtil;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String email = extractEmail(authentication);
        String nickname = extractNickname(authentication);
        Long id = extractUserId(authentication);
        String accessToken = jwtUtil.createAccessToken(email, nickname);
        String refreshToken = jwtUtil.createRefreshToken();

        jwtUtil.sendAccessAndRefreshToken(response, accessToken, refreshToken, id, nickname);
        jwtUtil.updateUserRefreshToken(email, refreshToken);

        log.info("login success. email: {}", email);
        log.info("refreshToken: {}", refreshToken);
        log.info("accessToken: {}", accessToken);
    }

    private String extractEmail(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    private String extractNickname(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getNickname();
    }

    private Long extractUserId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }




}
