package aico.backend.global.security;

import aico.backend.global.security.jwt.JwtUtil;
import aico.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().equals("/api/user/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtUtil.extractRefreshToken(request)
                                     .filter(jwtUtil::isTokenValid)
                                     .orElse(null);

        if(refreshToken != null) {
            reIssueAccessToken(response, refreshToken);
            return;
        }


    }

    private void reIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            jwtUtil.sendAccessToken(response, jwtUtil.createAccessToken(user.getEmail()));
        });
    }
}
