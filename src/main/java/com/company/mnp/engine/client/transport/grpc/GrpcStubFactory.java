package com.company.mnp.engine.client.transport.grpc;

import com.company.mnp.engine.client.model.EngineInstance;
import io.grpc.Channel;
import io.grpc.stub.AbstractStub;

import java.util.Objects;
import java.util.function.Function;

/** Creates generated asynchronous stubs backed by channels managed by the SDK. */
public final class GrpcStubFactory<S extends AbstractStub<S>> {
    private final GrpcChannelManager channelManager;
    private final Function<Channel, S> constructor;

    public GrpcStubFactory(GrpcChannelManager channelManager, Function<Channel, S> constructor) {
        this.channelManager = Objects.requireNonNull(channelManager, "channelManager");
        this.constructor = Objects.requireNonNull(constructor, "constructor");
    }

    public S create(EngineInstance engine) {
        Objects.requireNonNull(engine, "engine");
        return Objects.requireNonNull(
                constructor.apply(channelManager.getChannel(engine.endpoint())),
                "stub constructor returned null");
    }
}
