package com.company.mnp.engine.client.api;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** Per-call overrides. Unset values are supplied by client configuration. */
public final class EngineCallOptions {
    private static final EngineCallOptions DEFAULTS = builder().build();
    private final Duration timeout;
    private final AffinityMode affinityMode;
    private final String affinityKey;
    private final Integer maxAttempts;

    private EngineCallOptions(Builder b) {
        timeout = b.timeout;
        affinityMode = b.affinityMode;
        affinityKey = normalize(b.affinityKey);
        maxAttempts = b.maxAttempts;
        if (timeout != null && (timeout.isZero() || timeout.isNegative())) throw new IllegalArgumentException("timeout must be positive");
        if (maxAttempts != null && maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be at least 1");
        if (affinityMode == AffinityMode.REQUIRE && affinityKey == null) throw new IllegalArgumentException("affinityKey is required for REQUIRE mode");
    }

    public static EngineCallOptions defaults() { return DEFAULTS; }
    public static Builder builder() { return new Builder(); }
    public Optional<Duration> getTimeout() { return Optional.ofNullable(timeout); }
    public AffinityMode getAffinityMode() { return affinityMode; }
    public Optional<String> getAffinityKey() { return Optional.ofNullable(affinityKey); }
    public Optional<Integer> getMaxAttempts() { return Optional.ofNullable(maxAttempts); }

    private static String normalize(String value) {
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    public static final class Builder {
        private Duration timeout;
        private AffinityMode affinityMode = AffinityMode.NONE;
        private String affinityKey;
        private Integer maxAttempts;
        public Builder timeout(Duration value) { timeout = value; return this; }
        public Builder affinityMode(AffinityMode value) { affinityMode = Objects.requireNonNull(value, "affinityMode"); return this; }
        public Builder affinityKey(String value) { affinityKey = value; return this; }
        public Builder maxAttempts(int value) { maxAttempts = value; return this; }
        public EngineCallOptions build() { return new EngineCallOptions(this); }
    }
}
