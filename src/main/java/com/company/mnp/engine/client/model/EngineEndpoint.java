package com.company.mnp.engine.client.model;

import java.net.URI;
/** Network location of an engine. */
public record EngineEndpoint(String host, int port, boolean tls) {
    public EngineEndpoint {
        host = EngineId.requireText(host, "host");
        if (port < 1 || port > 65_535) throw new IllegalArgumentException("port must be between 1 and 65535");
    }

    public static EngineEndpoint plaintext(String host, int port) { return new EngineEndpoint(host, port, false); }
    public static EngineEndpoint tls(String host, int port) { return new EngineEndpoint(host, port, true); }
    public URI toUri() { return URI.create((tls ? "https" : "http") + "://" + host + ":" + port); }

    @Override public String toString() { return toUri().toString(); }
}
