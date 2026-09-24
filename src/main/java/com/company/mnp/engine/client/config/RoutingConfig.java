package com.company.mnp.engine.client.config;

import com.company.mnp.engine.client.api.AffinityMode;
import com.company.mnp.engine.client.model.SiteId;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** Defaults governing engine selection and affinity retention. */
public record RoutingConfig(AffinityMode affinityMode, Duration affinityTtl, SiteId preferredSite) {
    public RoutingConfig {
        Objects.requireNonNull(affinityMode, "affinityMode");
        GrpcConfig.positive(affinityTtl, "affinityTtl");
    }
    public static RoutingConfig defaults() { return new RoutingConfig(AffinityMode.NONE, Duration.ofMinutes(30), null); }
    public Optional<SiteId> preferredSiteOptional() { return Optional.ofNullable(preferredSite); }
}
