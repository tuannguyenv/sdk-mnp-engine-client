package com.company.mnp.engine.client.affinity;

import com.company.mnp.engine.client.model.EngineId;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe, in-memory affinity store for transactions and subscribers.
 *
 * <p>Transaction affinity has precedence over subscriber affinity. Entries expire
 * after the configured TTL, measured from the most recent bind. Expired entries
 * are rejected during lookup and can also be removed in bulk with
 * {@link #cleanupExpired()}.</p>
 */
public final class AffinityManager {
    private final Duration ttl;
    private final Clock clock;
    private final ConcurrentMap<String, AffinityEntry> transactionAffinities = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AffinityEntry> subscriberAffinities = new ConcurrentHashMap<>();

    public AffinityManager(Duration ttl) {
        this(ttl, Clock.systemUTC());
    }

    /** Constructor with an injectable clock, useful for deterministic expiry handling. */
    public AffinityManager(Duration ttl, Clock clock) {
        this.ttl = Objects.requireNonNull(ttl, "ttl");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Creates or refreshes a transaction-to-engine association. */
    public void bindTransaction(String transactionId, EngineId engineId) {
        bind(transactionAffinities, transactionId, "transactionId", engineId);
    }

    /** Creates or refreshes a subscriber-to-engine association. */
    public void bindSubscriber(String subscriberId, EngineId engineId) {
        bind(subscriberAffinities, subscriberId, "subscriberId", engineId);
    }

    /** Returns a live transaction affinity, removing it when it has expired. */
    public Optional<EngineId> findTransaction(String transactionId) {
        return find(transactionAffinities, transactionId, "transactionId");
    }

    /** Returns a live subscriber affinity, removing it when it has expired. */
    public Optional<EngineId> findSubscriber(String subscriberId) {
        return find(subscriberAffinities, subscriberId, "subscriberId");
    }

    /**
     * Resolves transaction affinity first and falls back to subscriber affinity.
     * Either identifier may be {@code null} or blank when it is unavailable.
     */
    public Optional<EngineId> resolve(String transactionId, String subscriberId) {
        Optional<EngineId> transaction = findIfPresent(transactionAffinities, transactionId);
        return transaction.isPresent() ? transaction : findIfPresent(subscriberAffinities, subscriberId);
    }

    public boolean removeTransaction(String transactionId) {
        return transactionAffinities.remove(requireKey(transactionId, "transactionId")) != null;
    }

    public boolean removeSubscriber(String subscriberId) {
        return subscriberAffinities.remove(requireKey(subscriberId, "subscriberId")) != null;
    }

    /** Removes all expired entries and returns the number removed. */
    public int cleanupExpired() {
        Instant now = clock.instant();
        return cleanup(transactionAffinities, now) + cleanup(subscriberAffinities, now);
    }

    public int transactionCount() {
        return transactionAffinities.size();
    }

    public int subscriberCount() {
        return subscriberAffinities.size();
    }

    /** Clears transaction and subscriber affinity state. */
    public void clear() {
        transactionAffinities.clear();
        subscriberAffinities.clear();
    }

    private void bind(ConcurrentMap<String, AffinityEntry> affinities, String key, String keyName,
                      EngineId engineId) {
        String normalizedKey = requireKey(key, keyName);
        Objects.requireNonNull(engineId, "engineId");
        affinities.put(normalizedKey, new AffinityEntry(engineId, clock.instant().plus(ttl)));
    }

    private Optional<EngineId> find(ConcurrentMap<String, AffinityEntry> affinities, String key,
                                    String keyName) {
        return findNormalized(affinities, requireKey(key, keyName));
    }

    private Optional<EngineId> findIfPresent(ConcurrentMap<String, AffinityEntry> affinities, String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return findNormalized(affinities, key.trim());
    }

    private Optional<EngineId> findNormalized(ConcurrentMap<String, AffinityEntry> affinities, String key) {
        AffinityEntry entry = affinities.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (!entry.isExpired(clock.instant())) {
            return Optional.of(entry.engineId());
        }
        affinities.remove(key, entry);
        return Optional.empty();
    }

    private static int cleanup(ConcurrentMap<String, AffinityEntry> affinities, Instant now) {
        int removed = 0;
        for (var entry : affinities.entrySet()) {
            if (entry.getValue().isExpired(now) && affinities.remove(entry.getKey(), entry.getValue())) {
                removed++;
            }
        }
        return removed;
    }

    private static String requireKey(String key, String name) {
        Objects.requireNonNull(key, name);
        String normalized = key.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    private record AffinityEntry(EngineId engineId, Instant expiresAt) {
        private boolean isExpired(Instant now) {
            return !now.isBefore(expiresAt);
        }
    }
}
