package com.company.mnp.engine.client.model;

import java.util.Map;
import java.util.Objects;

/** Immutable registry view of one engine process. */
public record EngineInstance(
        EngineId id,
        SiteId siteId,
        EngineEndpoint endpoint,
        EngineState state,
        int weight,
        Map<String, String> metadata) {

    public EngineInstance {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(siteId, "siteId");
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(state, "state");
        if (weight < 1) throw new IllegalArgumentException("weight must be at least 1");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
    }

    public EngineInstance(EngineId id, SiteId siteId, EngineEndpoint endpoint) {
        this(id, siteId, endpoint, EngineState.READY, 1, Map.of());
    }

    public boolean isRoutable() { return state.isRoutable(); }
}
