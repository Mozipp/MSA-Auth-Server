// AuthService.java
package com.example.auth.service;

import com.example.auth.dto.AuthResponseDto;
import com.example.auth.dto.LoginRequest;
import com.example.auth.entity.User;
import com.example.auth.util.CookieUtil;
import com.example.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

//    public void login(LoginRequest loginRequest, HttpServletResponse response) {
//        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())
//                .orElseThrow(() -> new RuntimeException("인증 실패"));
//        generateTokensAndSetCookies(user, response);
//    }

    public AuthResponseDto login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())
                .orElseThrow(() -> new RuntimeException("인증 실패"));

        // 토큰 및 세션 ID 생성
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String sessionId = UUID.randomUUID().toString();

        // Redis에 리프레시 토큰 저장
        tokenService.storeRefreshToken(sessionId, refreshToken, jwtUtil.getRefreshTokenExpiration());

        // 응답에 쿠키로 설정
        cookieUtil.addCookie(response, "access_token", accessToken, jwtUtil.getAccessTokenExpiration());
        cookieUtil.addCookie(response, "session_id", sessionId, jwtUtil.getRefreshTokenExpiration());

        // 응답 바디에도 토큰 정보 포함
        return new AuthResponseDto(accessToken, refreshToken, sessionId);
    }

    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = cookieUtil.getCookieValue(request, "session_id");
        if (sessionId == null) {
            throw new RuntimeException("세션 ID 없음");
        }

        String refreshToken = tokenService.getRefreshToken(sessionId);
        if (refreshToken == null) {
            throw new RuntimeException("리프레시 토큰 없음");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰");
        }

        Claims claims = jwtUtil.getClaimsFromToken(refreshToken);
        String username = claims.getSubject();

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        regenerateTokens(sessionId, user, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = cookieUtil.getCookieValue(request, "session_id");
        if (sessionId != null) {
            tokenService.deleteRefreshToken(sessionId);
            cookieUtil.deleteCookie(response, "access_token");
            cookieUtil.deleteCookie(response, "session_id");
        }
    }

    private void generateTokensAndSetCookies(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String sessionId = UUID.randomUUID().toString();

        tokenService.storeRefreshToken(sessionId, refreshToken, jwtUtil.getRefreshTokenExpiration());
        cookieUtil.addCookie(response, "access_token", accessToken, jwtUtil.getAccessTokenExpiration());
        cookieUtil.addCookie(response, "session_id", sessionId, jwtUtil.getRefreshTokenExpiration());
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