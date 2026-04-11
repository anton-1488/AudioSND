package org.plovdev.audioengine.utils;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

public class TrackUtils {
    private TrackUtils() {}

    /**
     * Converts the entire backing array of a ByteBuffer to a byte array.
     * <p>
     * <b>Note:</b> This method copies the <b>entire capacity</b> of the buffer,
     * not just the remaining data. Position and limit are ignored.
     * </p>
     *
     * @param buffer input ByteBuffer (position and limit are preserved)
     * @return byte array containing the full buffer capacity
     */
    public static byte[] byteBufferToByteArray(ByteBuffer buffer) {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.capacity()];
        copy.get(0, bytes);
        return bytes;
    }

    public static MemorySegment createMemorySegment(ByteBuffer src) {
        Arena arena = Arena.ofAuto();
        MemorySegment resultSegment = arena.allocate(src.capacity());
        resultSegment.asByteBuffer().put(src);
        return resultSegment;
    }
}