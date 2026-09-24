package com.company.mnp.engine.client.model;

/** Lifecycle state advertised by an engine registry. */
public enum EngineState {
    STARTING(false),
    READY(true),
    DRAINING(false),
    UNAVAILABLE(false),
    STOPPED(false);

    private final boolean routable;
    EngineState(boolean routable) { this.routable = routable; }
    public boolean isRoutable() { return routable; }
}
