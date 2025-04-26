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
    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();//5


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().equals("/api/user/login") || request.getRequestURI().equals("/api/user/signup")  || request.getRequestURI().equals("/api/user/duplicate") ) {
            filterChain.doFilter(request, response);
            return;
        }

        // 전달된 RefreshToken 이 없거나 유효하지않다면 null
        String refreshToken = jwtUtil.extractRefreshToken(request)
                                     .filter(jwtUtil::isTokenValid)
                                     .orElse(null);

        // 1. null 이 아니라면(RefreshToken valid) -> 액세스토큰 발급 후 바로 리턴(인증 처리 x)
        if(refreshToken != null) {
            reIssueAccessToken(response, refreshToken);
            return;
        }

        // 2. RefreshToken not valid -> 액세스 토큰이 유효한지 확인 후, 회원을 찾아와 인증 처리
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> accessToken = jwtUtil.extractAccessToken(request);

        if (accessToken.isEmpty() || !jwtUtil.isTokenValid(accessToken.get())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("not valid access token");
        } else {
            log.info("jwt 필터 호출됨");
            jwtUtil.extractEmail(accessToken.get())
                    .flatMap(userRepository::findByEmail)
                    .ifPresent(this::saveAuthentication);

            filterChain.doFilter(request,response);
        }

    }

    private void saveAuthentication(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private void reIssueAccessToken(HttpServletResponse response, String refreshToken) throws IOException {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("not valid refresh token");
            return;
        }
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            jwtUtil.sendAccessToken(response, jwtUtil.createAccessToken(user.getEmail(), user.getNickname()));
        });
    }
}
