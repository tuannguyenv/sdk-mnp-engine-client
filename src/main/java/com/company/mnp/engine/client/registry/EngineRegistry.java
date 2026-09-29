package com.company.mnp.engine.client.registry;

import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.EngineInstance;

import java.util.List;
import java.util.Optional;

/** Supplies immutable point-in-time views of the engines known to the client. */
public interface EngineRegistry {
    List<EngineInstance> getEngines();

    default Optional<EngineInstance> findById(EngineId id) {
        if (id == null) {
            return Optional.empty();
        }
        return getEngines().stream().filter(engine -> engine.id().equals(id)).findFirst();
    }
}
