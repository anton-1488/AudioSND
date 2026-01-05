package org.plovdev.audioengine.devices;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Базовый интерфейс для аудио устройств вывода (динамики, наушники).
 * Предоставляет методы для записи аудиоданных на устройство.
 *
 * @see AudioDevice
 * @see InputAudioDevice
 *
 * @version 1.0
 * @author Anton
 */
public interface OutputAudioDevice extends AudioDevice {
    int write(@NotNull ByteBuffer byteBuffer);
    void flush();
}