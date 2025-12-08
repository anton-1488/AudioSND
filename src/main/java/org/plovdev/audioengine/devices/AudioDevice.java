package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.exceptions.CloseAudioDeviceException;
import org.plovdev.audioengine.exceptions.OpenAudioDeviceExcpetion;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.Set;

/**
 * Самый абстрактный интерфейс аудио устройства. Нет ни ввода, ни вывода.
 * Это основа основ)
 */
public interface AudioDevice extends AutoCloseable {
    void open(TrackFormat format) throws OpenAudioDeviceExcpetion;
    String getName();
    Set<TrackFormat> getSupportedFormats();
    AudioDeviceStatus getDeviceStatus();
    @Override
    void close() throws CloseAudioDeviceException;
}