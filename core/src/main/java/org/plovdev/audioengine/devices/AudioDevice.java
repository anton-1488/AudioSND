package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.exceptions.CloseAudioDeviceException;
import org.plovdev.audioengine.exceptions.OpenAudioDeviceException;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.Set;

/**
 * Base interface for all audio devices (input or output).
 * Most abstractly device desctiption.
 * <p>
 * Devices follow this lifecycle:
 * 1. {@link #open(TrackFormat)} - allocate resources
 * 2. {@link #close()} - release resources
 * </p>
 *
 * @author Anton
 * @version 1.0
 */
public interface AudioDevice extends AutoCloseable {

    /**
     * Open audio device.
     * @param format working format
     * @throws OpenAudioDeviceException when opening failed.
     */
    void open(TrackFormat format) throws OpenAudioDeviceException;

    /**
     * Return system audio device name
     * @return name
     */
    String getName();

    /**
     * Return system audio device id
     * @return id
     */
    String getDeviceId();

    /**
     * Return formats, which audio device supported
     * @return supporteds format
     */
    Set<TrackFormat> getSupportedFormats();


    AudioDeviceStatus getDeviceStatus();

    /**
     * Checks if device is ready for I/O operations.
     */
    default boolean isReady() {
        return getDeviceStatus() == AudioDeviceStatus.OPENED;
    }

    @Override
    void close() throws CloseAudioDeviceException;
}