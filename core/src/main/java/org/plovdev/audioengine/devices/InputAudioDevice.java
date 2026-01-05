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
     * Read data from input audio device to buffer.
     *
     * @param byteBuffer buffer to read.
     * @return readed bytes.
     */
    int read(ByteBuffer byteBuffer);
}