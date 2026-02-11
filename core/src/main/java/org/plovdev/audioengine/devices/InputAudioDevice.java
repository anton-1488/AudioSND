package org.plovdev.audioengine.devices;

import java.nio.ByteBuffer;

/**
 * Base input audio device
 * Read data from driver(native)
 *
 * @version 1.0
 * @author Anton
 */
public interface InputAudioDevice extends AudioDevice {
    /**
     * Reads data from input audio device into the buffer.
     * Blocks until at least one byte is available or device is closed.
     *
     * @param byteBuffer buffer to read data into
     * @return number of bytes read, or -1 if end of stream/device closed
     * @throws IllegalStateException if device is not opened
     */
    int read(ByteBuffer byteBuffer);
}