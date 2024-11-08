// AuthService.java
package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.entity.User;
import com.example.auth.util.CookieUtil;
import com.example.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public Mono<Void> login(LoginRequest loginRequest, HttpServletResponse response) {
        return userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())
                .switchIfEmpty(Mono.error(new RuntimeException("인증 실패")))
                .flatMap(user -> generateTokensAndSetCookies(user, response));
    }

    public Mono<Void> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = cookieUtil.getCookieValue(request, "session_id");
        if (sessionId == null) {
            return Mono.error(new RuntimeException("세션 ID 없음"));
        }

        return tokenService.getRefreshToken(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("리프레시 토큰 없음")))
                .flatMap(refreshToken -> {
                    if (!jwtUtil.validateToken(refreshToken)) {
                        return Mono.error(new RuntimeException("유효하지 않은 리프레시 토큰"));
                    }

                    Claims claims = jwtUtil.getClaimsFromToken(refreshToken);
                    String username = claims.getSubject();

                    return userService.findByUsername(username)
                            .flatMap(user -> regenerateTokens(sessionId, user, response));
                });
    }

    public Mono<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = cookieUtil.getCookieValue(request, "session_id");
        if (sessionId == null) {
            return Mono.empty();
        }

        return tokenService.deleteRefreshToken(sessionId)
                .then(Mono.fromRunnable(() -> {
                    cookieUtil.deleteCookie(response, "access_token");
                    cookieUtil.deleteCookie(response, "session_id");
                }));
    }

    private Mono<Void> generateTokensAndSetCookies(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String sessionId = UUID.randomUUID().toString();

        return tokenService.storeRefreshToken(sessionId, refreshToken, jwtUtil.getRefreshTokenExpiration())
                .then(Mono.fromRunnable(() -> {
                    cookieUtil.addCookie(response, "access_token", accessToken, jwtUtil.getAccessTokenExpiration());
                    cookieUtil.addCookie(response, "session_id", sessionId, jwtUtil.getRefreshTokenExpiration());
                }));
    }

    private Mono<Void> regenerateTokens(String oldSessionId, User user, HttpServletResponse response) {
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String newSessionId = UUID.randomUUID().toString();

        return tokenService.storeRefreshToken(newSessionId, newRefreshToken, jwtUtil.getRefreshTokenExpiration())
                .then(tokenService.deleteRefreshToken(oldSessionId))
                .then(Mono.fromRunnable(() -> {
                    cookieUtil.addCookie(response, "access_token", newAccessToken, jwtUtil.getAccessTokenExpiration());
                    cookieUtil.addCookie(response, "session_id", newSessionId, jwtUtil.getRefreshTokenExpiration());
                }));
    }
}