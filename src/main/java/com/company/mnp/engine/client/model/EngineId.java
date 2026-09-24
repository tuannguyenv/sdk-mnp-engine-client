package com.company.mnp.engine.client.model;

import java.io.Serializable;

/** Stable identifier for an engine instance. */
public record EngineId(String value) implements Serializable {
    private static final long serialVersionUID = 1L;
    public EngineId {
        value = requireText(value, "value");
    }
    static String requireText(String value, String name) {
        if (value == null) throw new NullPointerException(name);
        String result = value.trim();
        if (result.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return result;
    }
    @Override public String toString() { return value; }
}
