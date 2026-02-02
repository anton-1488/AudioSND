package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;
import java.nio.ByteBuffer;

/**
 * Простой эффект эха: добавляет одну задержанную копию сигнала.
 */
public class EchoEffect implements AudioEffect {
    private final int delayMs;        // задержка в миллисекундах
    private final float feedback;      // коэффициент затухания (0.0–1.0)

    public EchoEffect(int delayMs, float feedback) {
        this.delayMs = delayMs;
        this.feedback = Math.max(0.0f, Math.min(1.0f, feedback)); // clamp 0–1
    }

    @Override
    public ByteBuffer process(TrackFormat format, ByteBuffer source) {
        int sampleRate = format.sampleRate(); // Гц (например, 44100)
        int bytesPerSample = format.bitDepth();
        int channels = format.channels();

        // Вычисляем количество образцов задержки
        int delaySamples = (int) ((delayMs * sampleRate) / 1000.0);
        int delayBytes = delaySamples * bytesPerSample * channels;

        ByteBuffer result = ByteBuffer.allocateDirect(source.capacity());
        source.rewind();

        while (source.hasRemaining()) {
            byte sample = source.get();
            result.put(sample);

            // Если есть место для задержанного сигнала — добавляем эхо
            if (source.position() >= delayBytes) {
                int echoPos = source.position() - delayBytes;
                if (echoPos < source.limit()) {
                    byte echoSample = source.get(echoPos);
                    // Применяем затухание и складываем с текущим
                    byte combined = (byte) (sample + (echoSample * feedback));
                    result.put(combined);
                }
            }
        }

        return result.flip();
    }
}
