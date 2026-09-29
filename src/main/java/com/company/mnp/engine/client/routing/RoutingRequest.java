package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.SiteId;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Immutable inputs used to route one logical engine call. */
public record RoutingRequest(
        String routingKey,
        SiteId preferredSite,
        RoutingPolicy policy,
        Set<EngineId> excludedEngineIds) {

    public RoutingRequest {
        Objects.requireNonNull(routingKey, "routingKey");
        if (routingKey.isBlank()) throw new IllegalArgumentException("routingKey must not be blank");
        routingKey = routingKey.trim();
        Objects.requireNonNull(policy, "policy");
        excludedEngineIds = Set.copyOf(Objects.requireNonNull(excludedEngineIds, "excludedEngineIds"));
        if (policy == RoutingPolicy.REQUIRE_SITE && preferredSite == null) {
            throw new IllegalArgumentException("preferredSite is required by REQUIRE_SITE policy");
        }
    }

    public RoutingRequest(String routingKey) {
        this(routingKey, null, RoutingPolicy.ANY, Set.of());
    }

    public Optional<SiteId> preferredSiteOptional() {
        return Optional.ofNullable(preferredSite);
    }
}
