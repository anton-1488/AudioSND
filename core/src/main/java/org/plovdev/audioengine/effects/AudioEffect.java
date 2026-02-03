package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;

/**
 * Класс эффекта. Должен обработать source буффер под реализованный эффект.
 */
public interface AudioEffect {
    /**
     * Инициализирует эффект(дает базовую настройку)
     * @param format формат трека
     */
    void setup(TrackFormat format);
    /**
     * Метод для обработки эффекта на буффер.
     * @param source исходный буфер
     * @return обработанный буфер
     */
    ByteBuffer process(ByteBuffer source);
}