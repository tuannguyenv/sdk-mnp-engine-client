package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.model.EngineInstance;

import java.util.Objects;

/** Selected engine and useful routing diagnostics. */
public record RoutingResult(EngineInstance engine, int candidateCount, boolean preferredSiteMatched) {
    public RoutingResult {
        Objects.requireNonNull(engine, "engine");
        if (candidateCount < 1) throw new IllegalArgumentException("candidateCount must be at least 1");
    }
}
