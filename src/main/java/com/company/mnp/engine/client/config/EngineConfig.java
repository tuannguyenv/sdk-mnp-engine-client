package com.company.mnp.engine.client.config;

import com.company.mnp.engine.client.model.EngineEndpoint;
import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.SiteId;

import java.util.Map;
import java.util.Objects;

/** Configuration for a statically registered engine. */
public record EngineConfig(
        EngineId id,
        SiteId siteId,
        EngineEndpoint endpoint,
        int weight,
        Map<String, String> metadata) {

    public EngineConfig {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(siteId, "siteId");
        Objects.requireNonNull(endpoint, "endpoint");
        if (weight < 1) throw new IllegalArgumentException("weight must be at least 1");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
    }
    public EngineConfig(EngineId id, SiteId siteId, EngineEndpoint endpoint) {
        this(id, siteId, endpoint, 1, Map.of());
    }
}
