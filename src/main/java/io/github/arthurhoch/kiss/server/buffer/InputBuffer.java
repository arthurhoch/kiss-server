package io.github.arthurhoch.kiss.server.buffer;

public final class InputBuffer {
    private final byte[] bytes;
    private int writeIndex;

    public InputBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.bytes = new byte[capacity];
    }

    public byte[] bytes() {
        return bytes;
    }

    public int writeIndex() {
        return writeIndex;
    }

    public void writeIndex(int writeIndex) {
        if (writeIndex < 0 || writeIndex > bytes.length) {
            throw new IllegalArgumentException("writeIndex out of bounds");
        }
        this.writeIndex = writeIndex;
    }

    public int remaining() {
        return bytes.length - writeIndex;
    }
}
