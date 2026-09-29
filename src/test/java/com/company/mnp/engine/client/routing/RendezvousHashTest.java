package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.model.EngineEndpoint;
import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.EngineInstance;
import com.company.mnp.engine.client.model.SiteId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RendezvousHashTest {
    private final RendezvousHash hash = new RendezvousHash();

    @Test
    void selectionIsStableRegardlessOfCandidateOrder() {
        EngineInstance one = engine("one", 1);
        EngineInstance two = engine("two", 1);

        EngineId forward = hash.select("subscriber-42", List.of(one, two)).orElseThrow().id();
        EngineId reverse = hash.select("subscriber-42", List.of(two, one)).orElseThrow().id();

        assertEquals(forward, reverse);
    }

    @Test
    void addingAnEngineOnlyMovesKeysToThatEngine() {
        List<EngineInstance> before = List.of(engine("one", 1), engine("two", 1));
        EngineInstance added = engine("three", 1);
        List<EngineInstance> after = List.of(before.get(0), before.get(1), added);

        IntStream.range(0, 500).forEach(index -> {
            EngineId oldSelection = hash.select("key-" + index, before).orElseThrow().id();
            EngineId newSelection = hash.select("key-" + index, after).orElseThrow().id();
            assertTrue(oldSelection.equals(newSelection) || added.id().equals(newSelection));
        });
    }

    @Test
    void distributesKeysAcrossCandidates() {
        List<EngineInstance> engines = List.of(engine("one", 1), engine("two", 1), engine("three", 1));
        Set<EngineId> selected = IntStream.range(0, 100)
                .mapToObj(index -> hash.select("key-" + index, engines).orElseThrow().id())
                .collect(Collectors.toSet());
        assertEquals(3, selected.size());
    }

    private static EngineInstance engine(String id, int weight) {
        return new EngineInstance(new EngineId(id), new SiteId("site-a"),
                EngineEndpoint.plaintext("localhost", 8000 + id.length()),
                com.company.mnp.engine.client.model.EngineState.READY, weight, java.util.Map.of());
    }
}
