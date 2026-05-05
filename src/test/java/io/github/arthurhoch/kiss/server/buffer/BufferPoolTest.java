package io.github.arthurhoch.kiss.server.buffer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BufferPoolTest {
    @Test
    void acquireReleaseAndReuseBuffers() {
        BufferPool pool = new BufferPool(8, 1);

        PooledBuffer first = pool.acquire();
        byte[] bytes = first.bytes();
        first.close();

        assertEquals(1, pool.pooledCount());
        PooledBuffer second = pool.acquire();
        assertSame(bytes, second.bytes());
    }

    @Test
    void poolDoesNotExceedMaxSize() {
        BufferPool pool = new BufferPool(8, 1);
        PooledBuffer first = pool.acquire();
        PooledBuffer second = pool.acquire();

        first.close();
        second.close();

        assertEquals(1, pool.pooledCount());
    }

    @Test
    void inputAndOutputBuffersValidateBounds() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new BufferPool(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new BufferPool(1, 0));
        InputBuffer input = new InputBuffer(4);
        input.writeIndex(2);
        assertEquals(2, input.remaining());
        assertThrows(IllegalArgumentException.class, () -> input.writeIndex(5));

        OutputBuffer output = new OutputBuffer(1);
        output.write((byte) 'A');
        assertThrows(IllegalStateException.class, () -> output.write((byte) 'B'));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        output.flushTo(bytes);
        assertEquals("A", bytes.toString());
        assertEquals(0, output.size());
    }
}
