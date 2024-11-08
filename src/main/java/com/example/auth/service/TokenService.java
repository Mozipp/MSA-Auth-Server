// TokenService.java
package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_KEY = "refresh_token:%s";

    public Mono<Void> storeRefreshToken(String sessionId, String refreshToken, long expiration) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        return redisTemplate.opsForValue()
                .set(key, refreshToken, Duration.ofMillis(expiration))
                .then();
    }

    public Mono<String> getRefreshToken(String sessionId) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Void> deleteRefreshToken(String sessionId) {
        String key = String.format(REFRESH_TOKEN_KEY, sessionId);
        return redisTemplate.delete(key).then();
    }
}