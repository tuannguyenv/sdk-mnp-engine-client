package com.company.mnp.engine.client.transport.grpc;

import com.company.mnp.engine.client.api.EngineRequest;
import io.grpc.stub.AbstractStub;

import java.util.concurrent.CompletionStage;

/** Adapts the SDK's transport-neutral request to a generated asynchronous gRPC stub call. */
@FunctionalInterface
public interface GrpcCall<S extends AbstractStub<S>> {
    CompletionStage<GrpcResponse> execute(S stub, EngineRequest request);
}
