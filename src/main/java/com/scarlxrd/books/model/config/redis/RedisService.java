package com.scarlxrd.books.model.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.kv;
@Slf4j
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

        log.info("Refresh token stored in Redis",
                kv("jti", jti),
                kv("ttl_seconds", ttlSeconds),
                kv("action", "save_refresh_token"));
    }

    public boolean isRefreshTokenValid(String jti){
        boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey("refresh:" + jti));
        if (!exists) {
            log.warn("Invalid or expired refresh token attempt",
                    kv("jti", jti),
                    kv("action", "validate_refresh_token"));
        }
        return exists;
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
        boolean blocked = Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jti));
        if (blocked) {
            log.warn("Revoked token access attempt",
                    kv("jti", jti),
                    kv("security_status", "blocked"));
        }
        return blocked;
    }
}
