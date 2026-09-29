package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.api.EngineErrorCode;
import com.company.mnp.engine.client.api.EngineException;
import com.company.mnp.engine.client.model.EngineEndpoint;
import com.company.mnp.engine.client.model.EngineId;
import com.company.mnp.engine.client.model.EngineInstance;
import com.company.mnp.engine.client.model.EngineState;
import com.company.mnp.engine.client.model.SiteId;
import com.company.mnp.engine.client.registry.StaticEngineRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultEngineRouterTest {
    private final EngineInstance siteA = engine("a", "site-a", EngineState.READY);
    private final EngineInstance siteB = engine("b", "site-b", EngineState.READY);
    private final EngineInstance unavailable = engine("down", "site-a", EngineState.UNAVAILABLE);
    private final DefaultEngineRouter router = new DefaultEngineRouter(
            new StaticEngineRegistry(List.of(siteA, siteB, unavailable)),
            (key, candidates) -> candidates.stream().findFirst());

    @Test
    void filtersUnavailableAndExcludedEngines() {
        RoutingRequest request = new RoutingRequest("key", null, RoutingPolicy.ANY, Set.of(siteA.id()));
        RoutingResult result = router.route(request);
        assertEquals(siteB.id(), result.engine().id());
        assertEquals(1, result.candidateCount());
    }

    @Test
    void prefersRequestedSite() {
        RoutingResult result = router.route(new RoutingRequest(
                "key", siteB.siteId(), RoutingPolicy.PREFER_SITE, Set.of()));
        assertEquals(siteB.id(), result.engine().id());
        assertTrue(result.preferredSiteMatched());
    }

    @Test
    void requireSiteFailsRatherThanFallingBack() {
        EngineException exception = assertThrows(EngineException.class, () -> router.route(new RoutingRequest(
                "key", new SiteId("unknown"), RoutingPolicy.REQUIRE_SITE, Set.of())));
        assertEquals(EngineErrorCode.NO_ENGINE_AVAILABLE, exception.getErrorCode());
    }

    private static EngineInstance engine(String id, String site, EngineState state) {
        return new EngineInstance(new EngineId(id), new SiteId(site),
                EngineEndpoint.plaintext("localhost", 8000 + id.length()), state, 1, Map.of());
    }
}
