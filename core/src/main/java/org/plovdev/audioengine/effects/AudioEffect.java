package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;

/**
 * Класс эффекта. Должен обработать source буффер под реализованный эффект.
 */
public interface AudioEffect {
    /**
     * Метод для обработки эффекта на буффер.
     * @param format формат трека
     * @param source исходный буффер
     * @return обработаный буффер
     */
    ByteBuffer process(TrackFormat format, ByteBuffer source);
}