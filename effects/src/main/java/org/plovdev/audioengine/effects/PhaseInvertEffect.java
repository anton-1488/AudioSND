package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;

/**
 * Эффект инверсии фазы: умножает все выборки на -1.
 */
public class PhaseInvertEffect implements AudioEffect {
    private TrackFormat format;

    /**
     * Инициализирует эффект(дает базовую настройку)
     *
     * @param format формат трека
     */
    @Override
    public void setup(TrackFormat format) {
        this.format = format;
    }

    @Override
    public ByteBuffer process(ByteBuffer source) {
        int frameSize = format.bytesPerSample();

        ByteBuffer result = ByteBuffer.allocateDirect(source.capacity());
        result.order(format.byteOrder());
        source.rewind();

        byte[] chunk = new byte[frameSize];
        for (int i = 0; i < source.limit(); i += frameSize) {
            source.get(chunk);
            for (byte sample : chunk) {
                byte inverted = (byte) (-sample);
                result.put(inverted);
            }
        }

        return result.flip();
    }
}
