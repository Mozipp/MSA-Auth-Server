// TokenService.java
package com.example.auth.service;

import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_KEY = "refresh_token:%s";

    public void storeRefreshToken(String sessionId, String refreshToken, long expiration) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        logger.info("Storing refresh token with session ID: {}", sessionId);
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(expiration));
    }

    public String getRefreshToken(String sessionId) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        String refreshToken = redisTemplate.opsForValue().get(key);
        logger.info("Retrieved refresh token for session ID: {}", sessionId);
        return refreshToken;
    }

    public void deleteRefreshToken(String sessionId) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        logger.info("Deleting refresh token for session ID: {}", sessionId);
        redisTemplate.delete(key);
    }
}