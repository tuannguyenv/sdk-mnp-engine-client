package com.company.mnp.engine.client.api;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/** Public entry point for invoking an MNP engine. Implementations must be thread-safe. */
public interface MnpEngineClient extends AutoCloseable {
    CompletionStage<EngineResult> executeAsync(EngineRequest request, EngineCallOptions options);

    default CompletionStage<EngineResult> executeAsync(EngineRequest request) {
        return executeAsync(Objects.requireNonNull(request, "request"), EngineCallOptions.defaults());
    }

    /** Utility for implementations that need to report an immediate validation failure. */
    static CompletionStage<EngineResult> failed(EngineException exception) {
        return CompletableFuture.failedFuture(Objects.requireNonNull(exception, "exception"));
    }

    @Override
    void close();
}
