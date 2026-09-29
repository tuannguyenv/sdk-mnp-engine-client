package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.api.EngineErrorCode;
import com.company.mnp.engine.client.api.EngineException;
import com.company.mnp.engine.client.model.EngineInstance;
import com.company.mnp.engine.client.registry.EngineRegistry;

import java.util.List;
import java.util.Objects;

/** Default router: filter unhealthy/excluded engines, apply site policy, then balance. */
public final class DefaultEngineRouter implements EngineRouter {
    private final EngineRegistry registry;
    private final LoadBalancer loadBalancer;

    public DefaultEngineRouter(EngineRegistry registry, LoadBalancer loadBalancer) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.loadBalancer = Objects.requireNonNull(loadBalancer, "loadBalancer");
    }

    @Override
    public RoutingResult route(RoutingRequest request) {
        Objects.requireNonNull(request, "request");
        List<EngineInstance> eligible = registry.getEngines().stream()
                .filter(EngineInstance::isRoutable)
                .filter(engine -> !request.excludedEngineIds().contains(engine.id()))
                .toList();

        List<EngineInstance> candidates = applySitePolicy(request, eligible);
        EngineInstance selected = loadBalancer.select(request.routingKey(), candidates)
                .orElseThrow(() -> noEngine(request));
        boolean siteMatched = request.preferredSite() != null
                && request.preferredSite().equals(selected.siteId());
        return new RoutingResult(selected, candidates.size(), siteMatched);
    }

    private static List<EngineInstance> applySitePolicy(RoutingRequest request, List<EngineInstance> eligible) {
        if (request.policy() == RoutingPolicy.ANY || request.preferredSite() == null) {
            return eligible;
        }
        List<EngineInstance> local = eligible.stream()
                .filter(engine -> request.preferredSite().equals(engine.siteId()))
                .toList();
        if (request.policy() == RoutingPolicy.REQUIRE_SITE || !local.isEmpty()) {
            return local;
        }
        return eligible;
    }

    private static EngineException noEngine(RoutingRequest request) {
        return new EngineException(
                EngineErrorCode.NO_ENGINE_AVAILABLE,
                "No routable engine is available for routing key '" + request.routingKey() + "'");
    }
}
