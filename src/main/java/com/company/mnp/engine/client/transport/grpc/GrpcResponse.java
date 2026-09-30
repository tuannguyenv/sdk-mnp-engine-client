package com.company.mnp.engine.client.transport.grpc;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/** Transport response returned by a generated-stub adapter. */
public final class GrpcResponse {
    private final byte[] payload;
    private final Map<String, String> metadata;

    public GrpcResponse(byte[] payload, Map<String, String> metadata) {
        this.payload = Arrays.copyOf(Objects.requireNonNull(payload, "payload"), payload.length);
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
    }

    public static GrpcResponse of(byte[] payload) {
        return new GrpcResponse(payload, Map.of());
    }

    public byte[] getPayload() {
        return Arrays.copyOf(payload, payload.length);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
