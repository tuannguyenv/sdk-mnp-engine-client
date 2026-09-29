package com.company.mnp.engine.client.routing;

/** Determines how a requested site constrains the candidate set. */
public enum RoutingPolicy {
    ANY,
    PREFER_SITE,
    REQUIRE_SITE
}
