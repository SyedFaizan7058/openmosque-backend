package com.openmosque.security.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token Bucket Rate Limiter Service.
 * Provides distributed rate limiting backed by Redis with a controlled,
 * memory-safe local fallback for development environments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RateLimitProperties properties;
    private final StringRedisTemplate redisTemplate;

    private final ConcurrentHashMap<String, LocalTokenBucket> localBuckets = new ConcurrentHashMap<>();

    private static final String REDIS_LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local capacity = tonumber(ARGV[1])\n" +
            "local refill_rate = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "local requested = 1\n" +
            "local data = redis.call('HMGET', key, 'tokens', 'last_refill')\n" +
            "local tokens = tonumber(data[1])\n" +
            "local last_refill = tonumber(data[2])\n" +
            "if not tokens then\n" +
            "    tokens = capacity\n" +
            "    last_refill = now\n" +
            "else\n" +
            "    local elapsed = math.max(0, now - last_refill)\n" +
            "    tokens = math.min(capacity, tokens + (elapsed * refill_rate))\n" +
            "    last_refill = now\n" +
            "end\n" +
            "local allowed = 0\n" +
            "local remaining = 0\n" +
            "local retry_after = 0\n" +
            "if tokens >= requested then\n" +
            "    tokens = tokens - requested\n" +
            "    allowed = 1\n" +
            "    remaining = math.floor(tokens)\n" +
            "else\n" +
            "    allowed = 0\n" +
            "    remaining = 0\n" +
            "    retry_after = math.ceil((requested - tokens) / refill_rate)\n" +
            "end\n" +
            "redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)\n" +
            "redis.call('EXPIRE', key, 120)\n" +
            "return {allowed, remaining, retry_after}";

    private final DefaultRedisScript<List> redisScript =
            new DefaultRedisScript<>(REDIS_LUA_SCRIPT, List.class);

    /**
     * Consumes 1 token from the bucket identified by 'key' using the Token Bucket algorithm.
     */
    public RateLimitResult tryConsume(String key, int capacity) {
        if (!properties.isEnabled()) {
            return RateLimitResult.builder()
                    .allowed(true)
                    .limit(capacity)
                    .remaining(capacity)
                    .resetSeconds(60)
                    .retryAfterSeconds(0)
                    .build();
        }

        if ("redis".equalsIgnoreCase(properties.getBackend())) {
            try {
                return tryConsumeRedis(key, capacity);
            } catch (Exception ex) {
                log.warn("Redis rate limiter failed for key '{}': {}. Applying failure policy: {}",
                        key, ex.getMessage(), properties.getRedisFailurePolicy());

                switch (properties.getRedisFailurePolicy()) {
                    case FAIL_OPEN:
                        return RateLimitResult.builder()
                                .allowed(true)
                                .limit(capacity)
                                .remaining(1)
                                .resetSeconds(60)
                                .retryAfterSeconds(0)
                                .build();
                    case FAIL_CLOSED:
                        return RateLimitResult.builder()
                                .allowed(false)
                                .limit(capacity)
                                .remaining(0)
                                .resetSeconds(60)
                                .retryAfterSeconds(60)
                                .build();
                    case LOCAL_FALLBACK:
                    default:
                        return tryConsumeLocal(key, capacity);
                }
            }
        }

        return tryConsumeLocal(key, capacity);
    }

    private RateLimitResult tryConsumeRedis(String key, int capacity) {
        double refillRatePerSec = (double) capacity / 60.0;
        long nowSeconds = System.currentTimeMillis() / 1000;

        List<?> result = redisTemplate.execute(
                redisScript,
                Collections.singletonList("rate-limit:" + key),
                String.valueOf(capacity),
                String.valueOf(refillRatePerSec),
                String.valueOf(nowSeconds)
        );

        if (result != null && result.size() >= 3) {
            boolean allowed = ((Number) result.get(0)).longValue() == 1;
            long remaining = ((Number) result.get(1)).longValue();
            long retryAfter = ((Number) result.get(2)).longValue();

            return RateLimitResult.builder()
                    .allowed(allowed)
                    .limit(capacity)
                    .remaining(Math.max(0, remaining))
                    .resetSeconds(allowed ? 60 : Math.max(1, retryAfter))
                    .retryAfterSeconds(allowed ? 0 : Math.max(1, retryAfter))
                    .build();
        }

        return tryConsumeLocal(key, capacity);
    }

    private RateLimitResult tryConsumeLocal(String key, int capacity) {
        LocalTokenBucket bucket = localBuckets.computeIfAbsent(key, k -> new LocalTokenBucket(capacity));
        return bucket.tryConsume();
    }

    /**
     * Prevents unbounded memory growth in development in-memory map.
     * Removes inactive buckets every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredLocalBuckets() {
        localBuckets.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * In-memory thread-safe Token Bucket implementation.
     */
    private static class LocalTokenBucket {
        private final double capacity;
        private final double refillRatePerMs;
        private double tokens;
        private long lastRefillMs;

        public LocalTokenBucket(int capacity) {
            this.capacity = capacity;
            this.refillRatePerMs = (double) capacity / 60000.0;
            this.tokens = capacity;
            this.lastRefillMs = System.currentTimeMillis();
        }

        public synchronized RateLimitResult tryConsume() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillMs;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + (elapsed * refillRatePerMs));
                lastRefillMs = now;
            }

            if (tokens >= 1.0) {
                tokens -= 1.0;
                long remaining = (long) Math.floor(tokens);
                long resetSeconds = (long) Math.ceil((capacity - tokens) / (refillRatePerMs * 1000.0));
                return RateLimitResult.builder()
                        .allowed(true)
                        .limit((long) capacity)
                        .remaining(Math.max(0, remaining))
                        .resetSeconds(Math.max(1, resetSeconds))
                        .retryAfterSeconds(0)
                        .build();
            } else {
                long retryAfter = (long) Math.ceil((1.0 - tokens) / (refillRatePerMs * 1000.0));
                return RateLimitResult.builder()
                        .allowed(false)
                        .limit((long) capacity)
                        .remaining(0)
                        .resetSeconds(Math.max(1, retryAfter))
                        .retryAfterSeconds(Math.max(1, retryAfter))
                        .build();
            }
        }

        public synchronized boolean isExpired() {
            return (System.currentTimeMillis() - lastRefillMs) > 180000;
        }
    }
}
