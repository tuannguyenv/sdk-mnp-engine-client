package com.company.mnp.engine.client.api;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineCallOptionsTest {
    @Test
    void requiredAffinityNeedsAKey() {
        assertThrows(IllegalArgumentException.class, () -> EngineCallOptions.builder()
                .affinityMode(AffinityMode.REQUIRE)
                .build());
    }

    @Test
    void acceptsValidatedOverrides() {
        EngineCallOptions options = EngineCallOptions.builder()
                .timeout(Duration.ofSeconds(2))
                .maxAttempts(2)
                .affinityMode(AffinityMode.REQUIRE)
                .affinityKey("subscriber-1")
                .build();

        assertEquals(Duration.ofSeconds(2), options.getTimeout().orElseThrow());
        assertEquals(2, options.getMaxAttempts().orElseThrow());
    }
}
