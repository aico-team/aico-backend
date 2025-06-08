package aico.backend.global.security.filter;

import aico.backend.global.security.JwtUtil;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import aico.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // (1) 로그인/회원가입/중복확인 같은 인증 예외 경로는 바로 통과
        if(request.getRequestURI().equals("/api/user/login") ||
                request.getRequestURI().equals("/api/user/signup")  ||
                request.getRequestURI().equals("/api/user/duplicate") ) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🥎 받은 요청 URL: {}", request.getRequestURI());

        // (2) Access Token 검사
        Optional<String> accessTokenOpt = jwtUtil.extractAccessToken(request);
        if (accessTokenOpt.isPresent() && jwtUtil.isTokenValid(accessTokenOpt.get())) {
            // ✅ Access Token이 유효하면 인증 정보 세팅 후, 원래 요청 처리
            jwtUtil.extractEmail(accessTokenOpt.get())
                    .flatMap(userRepository::findByEmail)
                    .ifPresent(this::saveAuthentication);

            filterChain.doFilter(request, response);
            return;
        }

        // (3) Access Token이 없거나 만료된 경우에만 Refresh Token 검증
        String refreshToken = jwtUtil.extractRefreshToken(request)
                .filter(jwtUtil::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            // 🔄 Access Token 재발급 로직
            reIssueAccessToken(response, refreshToken);
            return;
        }

        // (4) Access/Refresh 모두 없거나 유효하지 않으면 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("not valid access token");
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> accessToken = jwtUtil.extractAccessToken(request);

        if (accessToken.isEmpty() || !jwtUtil.isTokenValid(accessToken.get())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("not valid access token");
        } else {
            jwtUtil.extractEmail(accessToken.get())
                    .flatMap(userRepository::findByEmail)
                    .ifPresent(this::saveAuthentication);

            filterChain.doFilter(request,response);
        }

    }

    private void saveAuthentication(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private void reIssueAccessToken(HttpServletResponse response, String refreshToken) throws IOException {
        // Refresh Token 유효성은 이미 위에서 isTokenValid로 확인됨
        userRepository.findByRefreshToken(refreshToken).ifPresentOrElse(user -> {
            String newAccessToken = jwtUtil.createAccessToken(user.getEmail(), user.getNickname());
            jwtUtil.sendAccessToken(response, newAccessToken);
        }, () -> {
            try {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("not valid refresh token");
            } catch (IOException e) {
                log.error("Error writing unauthorized response: {}", e.getMessage());
            }
        });
    }
}
