// RedisConfig.java
package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;
import java.util.Arrays;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.cluster.nodes}")
    private String clusterNodes;

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean useSsl;

    @Value("${spring.data.redis.lettuce.shutdown-timeout}")
    private Duration shutdownTimeout;

    @Value("${spring.data.redis.cluster.max-redirects}")
    private int maxRedirects;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
                Arrays.asList(clusterNodes.split(","))
        );
        clusterConfig.setMaxRedirects(maxRedirects); // maxRedirects 설정 추가

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(clusterConfig);
        lettuceConnectionFactory.setUseSsl(useSsl);
        lettuceConnectionFactory.setShutdownTimeout(shutdownTimeout.toMillis());

        return lettuceConnectionFactory;
    }

    @Bean
    @Primary
    public ReactiveStringRedisTemplate reactiveRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }
}