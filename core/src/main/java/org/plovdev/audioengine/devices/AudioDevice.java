package org.plovdev.audioengine.devices;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.exceptions.devices.CloseAudioDeviceException;
import org.plovdev.audioengine.exceptions.devices.OpenAudioDeviceException;
import org.plovdev.audioengine.tracks.format.TrackFormat;

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
     * Open audio device with specified format.
     *
     * @param format working format
     * @throws OpenAudioDeviceException when opening failed.
     */
    void open(@NotNull TrackFormat format) throws OpenAudioDeviceException;

    /**
     * Check, supported audio device this format?
     *
     * @param format checking format.
     * @return is supported
     */
    default boolean isSupportedFormat(@NotNull TrackFormat format) {
        return getDeviceInfo().supportedFormats().contains(format);
    }

    /**
     * Return all info about audio device
     *
     * @return device info
     */
    AudioDeviceInfo getDeviceInfo();

    /**
     * Return status of audio device, which as opened, closed, etc.
     *
     * @return device status
     */
    AudioDeviceStatus getDeviceStatus();

    /**
     * Checks if device is ready for I/O operations.
     */
    default boolean isReady() {
        return getDeviceStatus() == AudioDeviceStatus.OPENED;
    }

    /**
     * Check if device open.
     * @return is device opened.
     */
    default boolean isOpen() {
        AudioDeviceStatus status = getDeviceStatus();
        return status == AudioDeviceStatus.OPENED ||
                status == AudioDeviceStatus.RUNNING;
    }

    @Override
    void close() throws CloseAudioDeviceException;
}