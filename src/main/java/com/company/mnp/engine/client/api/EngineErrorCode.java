package com.company.mnp.engine.client.api;

/** Stable, transport-independent error categories exposed to SDK users. */
public enum EngineErrorCode {
    INVALID_REQUEST(false),
    NO_ENGINE_AVAILABLE(true),
    DEADLINE_EXCEEDED(true),
    TRANSPORT_ERROR(true),
    ENGINE_UNAVAILABLE(true),
    ENGINE_REJECTED(false),
    INTERNAL_ERROR(false),
    CLIENT_CLOSED(false);

    private final boolean retryable;

    EngineErrorCode(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
