package org.plovdev.audioengine.utils;

import java.nio.ByteBuffer;

public class AudioEngineUtils {
    public static byte[] directBufferToBytes(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        } else {
            return buffer.array();
        }
    }
}