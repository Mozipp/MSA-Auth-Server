// TokenService.java
package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_KEY = "refresh_token:%s";

    public void storeRefreshToken(String sessionId, String refreshToken, long expiration) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(expiration));
    }

    public String getRefreshToken(String sessionId) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String sessionId) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        redisTemplate.delete(key);
    }
}