package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.model.EngineInstance;

import java.util.List;
import java.util.Optional;

/** Architecture-name adapter for {@link RendezvousHash}. */
public final class RendezvousHashLoadBalancer implements LoadBalancer {
    private final RendezvousHash delegate = new RendezvousHash();

    @Override
    public Optional<EngineInstance> select(String routingKey, List<EngineInstance> candidates) {
        return delegate.select(routingKey, candidates);
    }
}
