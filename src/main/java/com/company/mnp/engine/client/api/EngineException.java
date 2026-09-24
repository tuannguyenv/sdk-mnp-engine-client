package com.company.mnp.engine.client.api;

import com.company.mnp.engine.client.model.EngineId;

import java.util.Objects;
import java.util.Optional;

/** Exception raised when an engine call cannot produce a result. */
public class EngineException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final EngineErrorCode errorCode;
    private final EngineId engineId;

    public EngineException(EngineErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public EngineException(EngineErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public EngineException(EngineErrorCode errorCode, String message, EngineId engineId, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.engineId = engineId;
    }

    public EngineErrorCode getErrorCode() { return errorCode; }
    public Optional<EngineId> getEngineId() { return Optional.ofNullable(engineId); }
    public boolean isRetryable() { return errorCode.isRetryable(); }
}
