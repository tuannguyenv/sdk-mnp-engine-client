package com.company.mnp.engine.client.api;

import com.company.mnp.engine.client.model.EngineId;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/** Successful engine response plus call diagnostics. */
public final class EngineResult {
    private final byte[] payload;
    private final EngineId engineId;
    private final Duration elapsed;
    private final int attempts;
    private final Map<String, String> metadata;

    public EngineResult(byte[] payload, EngineId engineId, Duration elapsed, int attempts, Map<String, String> metadata) {
        this.payload = Arrays.copyOf(Objects.requireNonNull(payload, "payload"), payload.length);
        this.engineId = Objects.requireNonNull(engineId, "engineId");
        this.elapsed = Objects.requireNonNull(elapsed, "elapsed");
        if (elapsed.isNegative()) throw new IllegalArgumentException("elapsed must not be negative");
        if (attempts < 1) throw new IllegalArgumentException("attempts must be at least 1");
        this.attempts = attempts;
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
    }

    public byte[] getPayload() { return Arrays.copyOf(payload, payload.length); }
    public EngineId getEngineId() { return engineId; }
    public Duration getElapsed() { return elapsed; }
    public int getAttempts() { return attempts; }
    public Map<String, String> getMetadata() { return metadata; }
}
