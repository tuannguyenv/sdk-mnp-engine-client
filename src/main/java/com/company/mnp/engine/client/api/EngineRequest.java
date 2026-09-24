package com.company.mnp.engine.client.api;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable, transport-neutral request sent to an MNP engine. */
public final class EngineRequest {
    private final String operation;
    private final byte[] payload;
    private final Map<String, String> metadata;

    private EngineRequest(Builder b) {
        operation = requireText(b.operation, "operation");
        payload = Arrays.copyOf(b.payload, b.payload.length);
        metadata = Map.copyOf(b.metadata);
    }

    public static Builder builder(String operation) { return new Builder(operation); }
    public String getOperation() { return operation; }
    public byte[] getPayload() { return Arrays.copyOf(payload, payload.length); }
    public Map<String, String> getMetadata() { return metadata; }

    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }

    public static final class Builder {
        private final String operation;
        private byte[] payload = new byte[0];
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private Builder(String operation) { this.operation = operation; }
        public Builder payload(byte[] value) { payload = Arrays.copyOf(Objects.requireNonNull(value, "payload"), value.length); return this; }
        public Builder metadata(String key, String value) { metadata.put(requireText(key, "metadata key"), Objects.requireNonNull(value, "metadata value")); return this; }
        public Builder metadata(Map<String, String> values) { Objects.requireNonNull(values, "metadata").forEach(this::metadata); return this; }
        public EngineRequest build() { return new EngineRequest(this); }
    }
}
