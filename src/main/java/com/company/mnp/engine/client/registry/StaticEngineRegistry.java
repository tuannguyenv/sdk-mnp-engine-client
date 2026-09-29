package com.company.mnp.engine.client.registry;

import com.company.mnp.engine.client.model.EngineInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Thread-safe registry backed by a fixed engine snapshot. */
public final class StaticEngineRegistry implements EngineRegistry {
    private final List<EngineInstance> engines;

    public StaticEngineRegistry(List<EngineInstance> engines) {
        this.engines = List.copyOf(Objects.requireNonNull(engines, "engines"));
        if (this.engines.size() != new HashSet<>(this.engines.stream().map(EngineInstance::id).toList()).size()) {
            throw new IllegalArgumentException("engine IDs must be unique");
        }
    }

    @Override
    public List<EngineInstance> getEngines() {
        return engines;
    }
}
