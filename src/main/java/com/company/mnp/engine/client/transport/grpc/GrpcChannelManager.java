package com.company.mnp.engine.client.transport.grpc;

import com.company.mnp.engine.client.config.GrpcConfig;
import com.company.mnp.engine.client.model.EngineEndpoint;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.ChannelOption;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns and reuses one configured gRPC channel per engine endpoint. */
public final class GrpcChannelManager implements AutoCloseable {
    private static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(5);

    private final GrpcConfig config;
    private final Duration shutdownTimeout;
    private final ConcurrentMap<EngineEndpoint, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Object lifecycleLock = new Object();

    public GrpcChannelManager(GrpcConfig config) {
        this(config, DEFAULT_SHUTDOWN_TIMEOUT);
    }

    public GrpcChannelManager(GrpcConfig config, Duration shutdownTimeout) {
        this.config = Objects.requireNonNull(config, "config");
        this.shutdownTimeout = requirePositive(shutdownTimeout, "shutdownTimeout");
    }

    /** Returns the shared channel for an endpoint, creating it atomically when needed. */
    public ManagedChannel getChannel(EngineEndpoint endpoint) {
        Objects.requireNonNull(endpoint, "endpoint");
        synchronized (lifecycleLock) {
            ensureOpen();
            return channels.computeIfAbsent(endpoint, this::createChannel);
        }
    }

    /** Shuts down and removes a single endpoint channel. */
    public boolean removeChannel(EngineEndpoint endpoint) {
        Objects.requireNonNull(endpoint, "endpoint");
        ManagedChannel channel;
        synchronized (lifecycleLock) {
            channel = channels.remove(endpoint);
        }
        if (channel == null) {
            return false;
        }
        shutdown(channel);
        return true;
    }

    public int channelCount() {
        return channels.size();
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        ManagedChannel[] snapshot;
        synchronized (lifecycleLock) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            snapshot = channels.values().toArray(ManagedChannel[]::new);
            channels.clear();
        }
        for (ManagedChannel channel : snapshot) {
            channel.shutdown();
        }
        for (ManagedChannel channel : snapshot) {
            awaitTermination(channel);
        }
    }

    private ManagedChannel createChannel(EngineEndpoint endpoint) {
        ensureOpen();
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(endpoint.host(), endpoint.port())
                .keepAliveTime(saturatedNanos(config.keepAliveTime()), TimeUnit.NANOSECONDS)
                .keepAliveTimeout(saturatedNanos(config.keepAliveTimeout()), TimeUnit.NANOSECONDS)
                .keepAliveWithoutCalls(config.keepAliveWithoutCalls())
                .maxInboundMessageSize(config.maxInboundMessageBytes())
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, saturatedMillis(config.connectTimeout()));
        if (endpoint.tls()) {
            builder.useTransportSecurity();
        } else {
            builder.usePlaintext();
        }
        return builder.build();
    }

    private void shutdown(ManagedChannel channel) {
        channel.shutdown();
        awaitTermination(channel);
    }

    private void awaitTermination(ManagedChannel channel) {
        try {
            long timeoutNanos = saturatedNanos(shutdownTimeout);
            if (!channel.awaitTermination(timeoutNanos, TimeUnit.NANOSECONDS)) {
                channel.shutdownNow();
                channel.awaitTermination(timeoutNanos, TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException interrupted) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("gRPC channel manager is closed");
        }
    }

    private static int saturatedMillis(Duration duration) {
        long millis;
        try {
            millis = duration.toMillis();
        } catch (ArithmeticException overflow) {
            millis = Long.MAX_VALUE;
        }
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, millis));
    }

    private static long saturatedNanos(Duration duration) {
        try {
            return Math.max(1, duration.toNanos());
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static Duration requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return duration;
    }
}
