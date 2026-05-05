package io.github.arthurhoch.kiss.server.buffer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class PooledBuffer implements AutoCloseable {
    private final byte[] bytes;
    private final Consumer<byte[]> releaser;
    private final AtomicBoolean closed = new AtomicBoolean();

    PooledBuffer(byte[] bytes, Consumer<byte[]> releaser) {
        this.bytes = Objects.requireNonNull(bytes, "bytes");
        this.releaser = Objects.requireNonNull(releaser, "releaser");
    }

    public byte[] bytes() {
        return bytes;
    }

    public int length() {
        return bytes.length;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            releaser.accept(bytes);
        }
    }
}
