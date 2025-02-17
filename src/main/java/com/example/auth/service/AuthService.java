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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    // 로거 인스턴스 추가
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public void login(LoginRequest loginRequest, HttpServletResponse response) {
        logger.info("Authenticating user: {}", loginRequest.getUsername());
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())
                .orElseThrow(() -> new RuntimeException("인증 실패"));
        logger.info("User authenticated successfully: {}", user.getUsername());
        generateTokensAndSetCookies(user, response);
    }

    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Refreshing access token...");
        String sessionId = cookieUtil.getCookieValue(request, "session_id");
        if (sessionId == null) {
            logger.warn("Session ID is missing");
            throw new RuntimeException("세션 ID 없음");
        }

        String refreshToken = tokenService.getRefreshToken(sessionId);
        if (refreshToken == null) {
            logger.warn("Refresh token is missing for session ID: {}", sessionId);
            throw new RuntimeException("리프레시 토큰 없음");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            logger.warn("Invalid refresh token for session ID: {}", sessionId);
            throw new RuntimeException("유효하지 않은 리프레시 토큰");
        }

        Claims claims = jwtUtil.getClaimsFromToken(refreshToken);
        String username = claims.getSubject();
        logger.info("Refresh token validated for user: {}", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        regenerateTokens(sessionId, user, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Logging out user...");
        String sessionId = cookieUtil.getCookieValue(request, "session_id");
        if (sessionId != null) {
            tokenService.deleteRefreshToken(sessionId);
            logger.info("Deleted refresh token for session ID: {}", sessionId);
            cookieUtil.deleteCookie(response, "access_token");
            cookieUtil.deleteCookie(response, "session_id");
        }
    }

    private void generateTokensAndSetCookies(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String sessionId = UUID.randomUUID().toString();

        tokenService.storeRefreshToken(sessionId, refreshToken, jwtUtil.getRefreshTokenExpiration());

        try {
            cookieUtil.addCookie(response, "access_token", accessToken, jwtUtil.getAccessTokenExpiration());
            cookieUtil.addCookie(response, "session_id", sessionId, jwtUtil.getRefreshTokenExpiration());
        } catch (Exception e) {
            logger.error("Failed to set cookies: {}", e.getMessage());
            throw new RuntimeException("쿠키 설정 실패");
        }

        // 로그에 토큰 정보 출력 (보안 위험 주의)
        logger.info("User '{}' logged in. Access Token: {}, Refresh Token: {}, Session ID: {}",
                user.getUsername(), accessToken, refreshToken, sessionId);
    }

    private void regenerateTokens(String oldSessionId, User user, HttpServletResponse response) {
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String newSessionId = UUID.randomUUID().toString();

        tokenService.storeRefreshToken(newSessionId, newRefreshToken, jwtUtil.getRefreshTokenExpiration());
        tokenService.deleteRefreshToken(oldSessionId);
        cookieUtil.addCookie(response, "access_token", newAccessToken, jwtUtil.getAccessTokenExpiration());
        cookieUtil.addCookie(response, "session_id", newSessionId, jwtUtil.getRefreshTokenExpiration());
    }
}