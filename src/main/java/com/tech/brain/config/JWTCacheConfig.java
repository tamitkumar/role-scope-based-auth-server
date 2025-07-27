package com.tech.brain.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JWTCacheConfig {
    private final Map<String, CachedToken> tokenMap = new ConcurrentHashMap<>();

    public void put(String key, String token, Instant expiry) {
        tokenMap.put(key, new CachedToken(token, expiry));
    }

    public Optional<String> get(String key) {
        CachedToken cached = tokenMap.get(key);
        if (cached != null && cached.expiry.isAfter(Instant.now())) {
            return Optional.of(cached.token);
        }
        tokenMap.remove(key);
        return Optional.empty();
    }

    private static class CachedToken {
        String token;
        Instant expiry;

        CachedToken(String token, Instant expiry) {
            this.token = token;
            this.expiry = expiry;
        }
    }
}
