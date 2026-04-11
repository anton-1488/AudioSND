package org.plovdev.audioengine.devices;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

/**
 * Base interface for audio output devices (speakers, headphones).
 * Provides methods for writing audio data to the device.
 *
 * @see AudioDevice
 * @see InputAudioDevice
 *
 * @version 1.0
 * @author Anton
 */
public interface OutputAudioDevice extends AudioDevice {

    /**
     * Writes audio data from the buffer to the output device.
     * Blocks until at least one byte is written or device is closed.
     * The method may write fewer bytes than requested.
     *
     * @param byteBuffer buffer containing audio data to write
     * @return number of bytes actually written, or -1 if device is closed
     * @throws IllegalStateException if device is not opened
     */
    int write(@NotNull ByteBuffer byteBuffer);

    /**
     * Writes audio data directly from a memory segment.
     * Zero-copy path for memory-mapped files and off-heap buffers.
     *
     * @param segment memory segment containing audio data
     * @param start   starting offset in bytes
     * @param length  number of bytes to write
     * @return number of bytes actually written
     * @throws IllegalStateException if device is not opened
     */
    int write(@NotNull MemorySegment segment, long start, long length);
}