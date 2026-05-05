package io.github.arthurhoch.kiss.server.buffer;

import java.util.ArrayDeque;
import java.util.Queue;

public final class BufferPool {
    private final int bufferSize;
    private final int maxPooled;
    private final Queue<byte[]> pooled = new ArrayDeque<>();

    public BufferPool(int bufferSize, int maxPooled) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be positive");
        }
        if (maxPooled <= 0) {
            throw new IllegalArgumentException("maxPooled must be positive");
        }
        this.bufferSize = bufferSize;
        this.maxPooled = maxPooled;
    }

    public synchronized PooledBuffer acquire() {
        byte[] bytes = pooled.poll();
        if (bytes == null) {
            bytes = new byte[bufferSize];
        }
        return new PooledBuffer(bytes, this::release);
    }

    private synchronized void release(byte[] bytes) {
        if (bytes.length == bufferSize && pooled.size() < maxPooled) {
            pooled.offer(bytes);
        }
    }

    public synchronized int pooledCount() {
        return pooled.size();
    }
}
