package com.company.mnp.engine.client.affinity;

import com.company.mnp.engine.client.model.EngineId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AffinityManagerTest {
    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final AffinityManager manager = new AffinityManager(Duration.ofMinutes(5), clock);
    private final EngineId engineA = new EngineId("engine-a");
    private final EngineId engineB = new EngineId("engine-b");

    @Test
    void storesTransactionAndSubscriberAffinity() {
        manager.bindTransaction(" tx-1 ", engineA);
        manager.bindSubscriber(" subscriber-1 ", engineB);

        assertEquals(engineA, manager.findTransaction("tx-1").orElseThrow());
        assertEquals(engineB, manager.findSubscriber("subscriber-1").orElseThrow());
    }

    @Test
    void transactionAffinityTakesPrecedenceAndSubscriberIsFallback() {
        manager.bindSubscriber("subscriber-1", engineB);
        assertEquals(engineB, manager.resolve("missing", "subscriber-1").orElseThrow());

        manager.bindTransaction("tx-1", engineA);
        assertEquals(engineA, manager.resolve("tx-1", "subscriber-1").orElseThrow());
        assertTrue(manager.resolve(null, "subscriber-1").isPresent());
        assertTrue(manager.resolve(" ", "subscriber-1").isPresent());
    }

    @Test
    void rebindingRefreshesTtl() {
        manager.bindTransaction("tx-1", engineA);
        clock.advance(Duration.ofMinutes(4));
        manager.bindTransaction("tx-1", engineB);
        clock.advance(Duration.ofMinutes(2));

        assertEquals(engineB, manager.findTransaction("tx-1").orElseThrow());
    }

    @Test
    void lookupLazilyRemovesExpiredAffinity() {
        manager.bindTransaction("tx-1", engineA);
        clock.advance(Duration.ofMinutes(5));

        assertTrue(manager.findTransaction("tx-1").isEmpty());
        assertEquals(0, manager.transactionCount());
    }

    @Test
    void cleanupRemovesExpiredEntriesFromBothScopes() {
        manager.bindTransaction("tx-old", engineA);
        manager.bindSubscriber("subscriber-old", engineA);
        clock.advance(Duration.ofMinutes(4));
        manager.bindTransaction("tx-current", engineB);
        clock.advance(Duration.ofMinutes(1));

        assertEquals(2, manager.cleanupExpired());
        assertEquals(1, manager.transactionCount());
        assertEquals(0, manager.subscriberCount());
        assertEquals(engineB, manager.findTransaction("tx-current").orElseThrow());
    }

    @Test
    void supportsExplicitRemovalAndClear() {
        manager.bindTransaction("tx-1", engineA);
        manager.bindSubscriber("subscriber-1", engineB);

        assertTrue(manager.removeTransaction("tx-1"));
        assertFalse(manager.removeTransaction("tx-1"));
        manager.clear();
        assertEquals(0, manager.subscriberCount());
    }

    @Test
    void validatesTtlKeysAndEngine() {
        assertThrows(IllegalArgumentException.class,
                () -> new AffinityManager(Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> manager.bindTransaction(" ", engineA));
        assertThrows(NullPointerException.class,
                () -> manager.bindSubscriber("subscriber-1", null));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
