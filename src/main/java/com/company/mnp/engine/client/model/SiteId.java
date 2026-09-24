package com.company.mnp.engine.client.model;

/** Datacenter or deployment-site identifier used by routing policies. */
public record SiteId(String value) {
    public SiteId {
        value = EngineId.requireText(value, "value");
    }
    @Override public String toString() { return value; }
}
