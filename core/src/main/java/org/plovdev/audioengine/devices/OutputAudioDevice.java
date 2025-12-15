package org.plovdev.audioengine.devices;

import java.nio.ByteBuffer;

/**
 * Base output device
 * Write data to driver
 */
public interface OutputAudioDevice extends AudioDevice {
    int write(ByteBuffer byteBuffer);
    void flush();
}