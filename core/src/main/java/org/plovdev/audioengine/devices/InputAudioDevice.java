package org.plovdev.audioengine.devices;

import java.nio.ByteBuffer;

/**
 * Base input audio device
 * Read data from driver(native)
 */
public interface InputAudioDevice extends AudioDevice {
    int read(ByteBuffer byteBuffer);
    int read(ByteBuffer byteBuffer, int start, int end);
}