package com.scarlxrd.books.model.config.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // salve refresh token valido
    public void saveRefreshToken(String jti, long ttlSeconds){
        // evitar passar TTL negativo ao Redis.
        ttlSeconds = Math.max(ttlSeconds, 0);
        redisTemplate.opsForValue().set("refresh:" + jti, "valid", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isRefreshTokenValid(String jti){
        return Boolean.TRUE.equals(redisTemplate.hasKey("refresh:" + jti));
    }

    public void deleteRefreshToken(String jti){
        redisTemplate.delete("refresh:" + jti);
    }

    // salva o access token na blackList
    public void blackListToken(String jti, long ttlSeconds){
        ttlSeconds = Math.max(ttlSeconds, 0);
        redisTemplate.opsForValue().set("blacklist:" + jti, "revoked", ttlSeconds, TimeUnit.SECONDS);
    }
    public boolean isBlackListed(String jti){
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jti));
    }
}
