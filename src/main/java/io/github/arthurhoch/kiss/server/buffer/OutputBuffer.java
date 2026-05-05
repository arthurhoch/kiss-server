package io.github.arthurhoch.kiss.server.buffer;

import java.io.IOException;
import java.io.OutputStream;

public final class OutputBuffer {
    private final byte[] bytes;
    private int size;

    public OutputBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.bytes = new byte[capacity];
    }

    public void write(byte value) {
        if (size >= bytes.length) {
            throw new IllegalStateException("output buffer is full");
        }
        bytes[size++] = value;
    }

    public int size() {
        return size;
    }

    public void flushTo(OutputStream output) throws IOException {
        output.write(bytes, 0, size);
        size = 0;
    }
}
