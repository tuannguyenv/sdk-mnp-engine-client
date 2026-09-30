package com.company.mnp.engine.client.transport.grpc;

import com.company.mnp.engine.client.api.EngineErrorCode;
import com.company.mnp.engine.client.api.EngineException;
import com.company.mnp.engine.client.api.EngineRequest;
import com.company.mnp.engine.client.api.EngineResult;
import com.company.mnp.engine.client.model.EngineInstance;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.AbstractStub;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Executes one engine attempt through a generated asynchronous gRPC stub. */
public final class GrpcEngineTransport<S extends AbstractStub<S>> {
    private final GrpcStubFactory<S> stubFactory;
    private final GrpcCall<S> call;
    private final LongSupplier nanoTime;

    public GrpcEngineTransport(GrpcStubFactory<S> stubFactory, GrpcCall<S> call) {
        this(stubFactory, call, System::nanoTime);
    }

    GrpcEngineTransport(GrpcStubFactory<S> stubFactory, GrpcCall<S> call, LongSupplier nanoTime) {
        this.stubFactory = Objects.requireNonNull(stubFactory, "stubFactory");
        this.call = Objects.requireNonNull(call, "call");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    /**
     * Executes a single transport attempt. Retry orchestration remains the responsibility of
     * the client layer, so successful results produced here always report one attempt.
     */
    public CompletionStage<EngineResult> executeAsync(
            EngineInstance engine, EngineRequest request, Duration timeout) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(request, "request");
        requirePositive(timeout, "timeout");

        long startedAt = nanoTime.getAsLong();
        S stub;
        CompletionStage<GrpcResponse> response;
        try {
            stub = stubFactory.create(engine)
                    .withDeadlineAfter(saturatedNanos(timeout), TimeUnit.NANOSECONDS);
            response = Objects.requireNonNull(call.execute(stub, request), "gRPC call returned null");
        } catch (Throwable failure) {
            return CompletableFuture.failedFuture(mapFailure(failure, engine));
        }

        return response.handle((value, failure) -> {
            if (failure != null) {
                throw new CompletionException(mapFailure(failure, engine));
            }
            GrpcResponse grpcResponse = Objects.requireNonNull(value, "gRPC response");
            long elapsedNanos = Math.max(0, nanoTime.getAsLong() - startedAt);
            return new EngineResult(grpcResponse.getPayload(), engine.id(),
                    Duration.ofNanos(elapsedNanos), 1, grpcResponse.getMetadata());
        });
    }

    private static EngineException mapFailure(Throwable failure, EngineInstance engine) {
        Throwable cause = unwrap(failure);
        if (cause instanceof EngineException engineException) {
            return engineException;
        }
        Status status = statusOf(cause);
        EngineErrorCode code = switch (status.getCode()) {
            case DEADLINE_EXCEEDED -> EngineErrorCode.DEADLINE_EXCEEDED;
            case UNAVAILABLE, RESOURCE_EXHAUSTED, ABORTED -> EngineErrorCode.ENGINE_UNAVAILABLE;
            case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE, ALREADY_EXISTS ->
                    EngineErrorCode.ENGINE_REJECTED;
            default -> EngineErrorCode.TRANSPORT_ERROR;
        };
        String description = status.getDescription();
        String message = "gRPC call to engine '" + engine.id() + "' failed with " + status.getCode()
                + (description == null ? "" : ": " + description);
        return new EngineException(code, message, engine.id(), cause);
    }

    private static Status statusOf(Throwable failure) {
        if (failure instanceof StatusException statusException) {
            return statusException.getStatus();
        }
        if (failure instanceof StatusRuntimeException statusException) {
            return statusException.getStatus();
        }
        return Status.fromThrowable(failure);
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable result = failure;
        while ((result instanceof CompletionException
                || result instanceof java.util.concurrent.ExecutionException)
                && result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static long saturatedNanos(Duration duration) {
        try {
            return duration.toNanos();
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }
}
