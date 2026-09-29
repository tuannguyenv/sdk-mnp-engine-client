package com.company.mnp.engine.client.routing;

import com.company.mnp.engine.client.model.EngineInstance;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Weighted highest-random-weight (rendezvous) hashing.
 * The same key and candidate set always select the same engine, while changes to
 * the set only remap keys that are affected by the change.
 */
public final class RendezvousHash implements LoadBalancer {
    private static final String HASH_ALGORITHM = "SHA-256";

    @Override
    public Optional<EngineInstance> select(String routingKey, List<EngineInstance> candidates) {
        Objects.requireNonNull(routingKey, "routingKey");
        Objects.requireNonNull(candidates, "candidates");
        return candidates.stream()
                .peek(candidate -> Objects.requireNonNull(candidate, "candidate"))
                .max(Comparator.<EngineInstance>comparingDouble(candidate -> score(routingKey, candidate))
                        .thenComparing(candidate -> candidate.id().value()));
    }

    double score(String routingKey, EngineInstance engine) {
        MessageDigest digest = newDigest();
        digest.update(routingKey.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        byte[] hash = digest.digest(engine.id().value().getBytes(StandardCharsets.UTF_8));
        long bits = ByteBuffer.wrap(hash).getLong();
        double uniform = ((bits >>> 11) + 1.0d) / ((1L << 53) + 1.0d);
        return engine.weight() / -Math.log(uniform);
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(HASH_ALGORITHM + " is required by the Java platform", exception);
        }
    }
}
