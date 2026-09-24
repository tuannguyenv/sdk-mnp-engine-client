package com.company.mnp.engine.client.config;

import com.company.mnp.engine.client.model.EngineEndpoint;
import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.SiteId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineClientConfigTest {
    @Test
    void defaultsAreUsable() {
        EngineClientConfig config = EngineClientConfig.builder().build();
        assertEquals(3, config.getMaxAttempts());
        assertEquals(4 * 1024 * 1024, config.getGrpc().maxInboundMessageBytes());
    }

    @Test
    void rejectsDuplicateEngineIds() {
        EngineConfig first = engine("same", 8080);
        EngineConfig second = engine("same", 8081);
        assertThrows(IllegalArgumentException.class, () -> EngineClientConfig.builder()
                .addEngine(first)
                .addEngine(second)
                .build());
    }

    private static EngineConfig engine(String id, int port) {
        return new EngineConfig(new EngineId(id), new SiteId("site-a"), EngineEndpoint.plaintext("localhost", port));
    }
}
