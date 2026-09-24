package com.company.mnp.engine.client.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineRequestTest {
    @Test
    void defensivelyCopiesPayloadAndMetadata() {
        byte[] bytes = {1, 2};
        EngineRequest request = EngineRequest.builder("  quote ")
                .payload(bytes)
                .metadata("trace-id", "123")
                .build();

        bytes[0] = 9;
        byte[] returned = request.getPayload();
        returned[1] = 9;

        assertEquals("quote", request.getOperation());
        assertArrayEquals(new byte[]{1, 2}, request.getPayload());
        assertThrows(UnsupportedOperationException.class, () -> request.getMetadata().put("x", "y"));
    }
}
