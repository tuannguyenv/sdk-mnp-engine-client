package com.company.mnp.engine.client.config;

import java.time.Duration;
import java.util.Objects;

/** gRPC channel tuning shared by all engine connections. */
public record GrpcConfig(
        Duration connectTimeout,
        Duration keepAliveTime,
        Duration keepAliveTimeout,
        int maxInboundMessageBytes,
        boolean keepAliveWithoutCalls) {

    public GrpcConfig {
        positive(connectTimeout, "connectTimeout");
        positive(keepAliveTime, "keepAliveTime");
        positive(keepAliveTimeout, "keepAliveTimeout");
        if (maxInboundMessageBytes < 1) throw new IllegalArgumentException("maxInboundMessageBytes must be positive");
    }

    public static GrpcConfig defaults() {
        return new GrpcConfig(Duration.ofSeconds(5), Duration.ofSeconds(30), Duration.ofSeconds(10), 4 * 1024 * 1024, false);
    }

    static void positive(Duration value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isZero() || value.isNegative()) throw new IllegalArgumentException(name + " must be positive");
    }
}
