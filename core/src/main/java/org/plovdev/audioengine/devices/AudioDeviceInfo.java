package org.plovdev.audioengine.devices;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.List;
import java.util.Objects;

/**
 * Класс-описание конкретного аудио устройства.
 *
 * @param id id устройства
 * @param name имя устройства
 * @param vendor производитель устройства
 * @param type тип устройства(на вход, выход, или дуплексное)
 * @param supportedFormats поддерживаемые устройством форматы
 *
 * @see TrackFormat
 *
 * @version 1.0
 * @author Anton
 */
public record AudioDeviceInfo(@NotNull String id, String name, String vendor, @NotNull AudioDeviceType type, @NotNull List<TrackFormat> supportedFormats) {
    public AudioDeviceInfo {
        supportedFormats = List.copyOf(supportedFormats);
    }

    public enum AudioDeviceType {
        /** Устройство ввода (микрофон, линейный вход) */
        INPUT,

        /** Устройство вывода (динамики, колонки, наушники) */
        OUTPUT,

        /** Полнодуплексное устройство (ввод и вывод одновременно) */
        DUPLEX
    }

    @Override
    public String toString() {
        return String.format("%s(%s) powered by %s. AudioDevice pictureType: %s, Supports formats: %s", name(), id(), vendor().replace(".", ""), type.name(), supportedFormats().size());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AudioDeviceInfo that = (AudioDeviceInfo) o;
        return Objects.equals(id, that.id);
    }
}