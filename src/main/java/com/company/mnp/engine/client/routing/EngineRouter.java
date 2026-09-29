package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.api.EngineException;

/** Resolves an engine for a logical call or raises an {@link EngineException}. */
@FunctionalInterface
public interface EngineRouter {
    RoutingResult route(RoutingRequest request);
}
