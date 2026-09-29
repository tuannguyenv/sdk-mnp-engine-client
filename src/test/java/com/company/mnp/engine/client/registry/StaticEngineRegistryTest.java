package com.company.mnp.engine.client.registry;

import com.company.mnp.engine.client.model.EngineEndpoint;
import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.EngineInstance;
import com.company.mnp.engine.client.model.SiteId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StaticEngineRegistryTest {
    @Test
    void snapshotsInputAndLooksUpById() {
        EngineInstance engine = engine("one");
        StaticEngineRegistry registry = new StaticEngineRegistry(List.of(engine));
        assertEquals(engine, registry.findById(engine.id()).orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> registry.getEngines().clear());
    }

    @Test
    void rejectsDuplicateIds() {
        assertThrows(IllegalArgumentException.class,
                () -> new StaticEngineRegistry(List.of(engine("one"), engine("one"))));
    }

    private static EngineInstance engine(String id) {
        return new EngineInstance(new EngineId(id), new SiteId("site-a"),
                EngineEndpoint.plaintext("localhost", 8080));
    }
}
