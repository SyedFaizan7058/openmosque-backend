package com.openmosque.security.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private String backend = "redis"; // "redis" or "local"
    private RedisFailurePolicy redisFailurePolicy = RedisFailurePolicy.LOCAL_FALLBACK;
    private List<String> trustedProxies = new ArrayList<>();

    // Limits in tokens per minute
    private int publicReadLimit = 100;
    private int authenticatedLimit = 120;
    private int sensitiveWriteLimit = 15;

    public enum RedisFailurePolicy {
        LOCAL_FALLBACK,
        FAIL_OPEN,
        FAIL_CLOSED
    }
}
