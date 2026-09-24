package com.company.mnp.engine.client.config;

import java.time.Duration;

/** Active/passive health-check thresholds. */
public record HealthConfig(
        boolean activeChecksEnabled,
        Duration checkInterval,
        Duration checkTimeout,
        int unhealthyThreshold,
        int healthyThreshold) {

    public HealthConfig {
        GrpcConfig.positive(checkInterval, "checkInterval");
        GrpcConfig.positive(checkTimeout, "checkTimeout");
        if (unhealthyThreshold < 1) throw new IllegalArgumentException("unhealthyThreshold must be at least 1");
        if (healthyThreshold < 1) throw new IllegalArgumentException("healthyThreshold must be at least 1");
    }
    public static HealthConfig defaults() { return new HealthConfig(true, Duration.ofSeconds(10), Duration.ofSeconds(2), 3, 2); }
}
