package aico.backend.global.security;

import aico.backend.user.domain.User;
import aico.backend.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access.expiration}")
    private int accessExpiration;
    @Value("${jwt.refresh.expiration}")
    private int refreshExpiration;
    @Value("${jwt.access.header}")
    private String accessHeader;

    private static final String BEARER = "Bearer";

    private final UserRepository userRepository;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(String email, String nickname) {
        return Jwts.builder()
                .setSubject("AccessToken")
                .claim("email", email)
                .claim("nickname", nickname)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Transactional
    public String createRefreshToken() {
        return Jwts.builder()
                .setSubject("RefreshToken")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Transactional
    public void updateUserRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> {throw new UsernameNotFoundException("user not found");}
                );
    }

    @Transactional
    public void destroyRefreshToken(String email) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        User::destroyRefreshToken,
                        () -> {throw new UsernameNotFoundException("user not found");}
                );
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken,
                                          Long id, String nickname) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        setRefreshTokenCookie(response, refreshToken);

        // AccessToken을 JSON으로 응답
        String json = String.format("""
                {"accessToken": "%s",
                "userId:": "%s",
                "nickname": "%s"}""", accessToken, id, nickname);
        response.getWriter().write(json);
    }

    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, BEARER + accessToken);
    }

    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        setAccessTokenHeader(response, accessToken);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(refreshExpiration);
        response.addCookie(cookie);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.ofNullable(claims.get("email", String.class));
        } catch (JwtException e) {
            log.error("parsing error: {}", e.getMessage());
            return Optional.empty();
        }
    }


    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("not valid token: {}", e.getMessage());
            return false;
        }
    }


}
