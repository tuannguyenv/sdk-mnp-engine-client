package com.company.mnp.engine.client.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Root immutable configuration for an engine client. */
public final class EngineClientConfig {
    private final List<EngineConfig> engines;
    private final RoutingConfig routing;
    private final GrpcConfig grpc;
    private final HealthConfig health;
    private final Duration defaultTimeout;
    private final int maxAttempts;

    private EngineClientConfig(Builder b) {
        engines = List.copyOf(b.engines);
        routing = b.routing;
        grpc = b.grpc;
        health = b.health;
        defaultTimeout = b.defaultTimeout;
        maxAttempts = b.maxAttempts;
        GrpcConfig.positive(defaultTimeout, "defaultTimeout");
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be at least 1");
        long distinctIds = engines.stream().map(EngineConfig::id).distinct().count();
        if (distinctIds != engines.size()) throw new IllegalArgumentException("engine IDs must be unique");
    }

    public static Builder builder() { return new Builder(); }
    public List<EngineConfig> getEngines() { return engines; }
    public RoutingConfig getRouting() { return routing; }
    public GrpcConfig getGrpc() { return grpc; }
    public HealthConfig getHealth() { return health; }
    public Duration getDefaultTimeout() { return defaultTimeout; }
    public int getMaxAttempts() { return maxAttempts; }

    public static final class Builder {
        private final List<EngineConfig> engines = new ArrayList<>();
        private RoutingConfig routing = RoutingConfig.defaults();
        private GrpcConfig grpc = GrpcConfig.defaults();
        private HealthConfig health = HealthConfig.defaults();
        private Duration defaultTimeout = Duration.ofSeconds(10);
        private int maxAttempts = 3;
        public Builder addEngine(EngineConfig value) { engines.add(Objects.requireNonNull(value, "engine")); return this; }
        public Builder engines(List<EngineConfig> values) { engines.clear(); Objects.requireNonNull(values, "engines").forEach(this::addEngine); return this; }
        public Builder routing(RoutingConfig value) { routing = Objects.requireNonNull(value, "routing"); return this; }
        public Builder grpc(GrpcConfig value) { grpc = Objects.requireNonNull(value, "grpc"); return this; }
        public Builder health(HealthConfig value) { health = Objects.requireNonNull(value, "health"); return this; }
        public Builder defaultTimeout(Duration value) { defaultTimeout = value; return this; }
        public Builder maxAttempts(int value) { maxAttempts = value; return this; }
        public EngineClientConfig build() { return new EngineClientConfig(this); }
    }
}
