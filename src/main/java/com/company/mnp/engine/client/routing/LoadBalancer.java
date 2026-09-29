package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.model.EngineInstance;

import java.util.List;
import java.util.Optional;

/** Selects one engine from an already-filtered candidate list. */
@FunctionalInterface
public interface LoadBalancer {
    Optional<EngineInstance> select(String routingKey, List<EngineInstance> candidates);
}
