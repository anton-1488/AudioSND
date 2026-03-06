package org.plovdev.audioengine.generator.strategies.noise;

import java.util.Random;

/**
 * Качественная реализация коричневого шума (1/f^2).
 * Использует leaky integrator для предотвращения DC-смещения и клиппинга.
 */
public class BrownNoise implements NoiseStrategy {
    private final Random random = new Random();
    private final float[] lastSamples;

    // Коэффициент утечки, предотвращающий "уход" сигнала в бесконечность
    private static final float LEAK_FACTOR = 0.99f;
    // Масштабирующий коэффициент для поддержания громкости
    private static final float GAIN = 0.05f;

    public BrownNoise(int channels) {
        this.lastSamples = new float[channels];
    }
    public BrownNoise() {
        this.lastSamples = new float[2];
    }

    @Override
    public float nextSample(int channel) {
        // 1. Генерируем белый шум
        float white = (random.nextFloat() * 2.0f - 1.0f);

        // 2. Применяем формулу: y[n] = (leak * y[n-1]) + white
        float brown = (LEAK_FACTOR * lastSamples[channel]) + (GAIN * white);

        // 3. Ограничиваем (clamping), чтобы избежать искажений
        if (brown > 1.0f) brown = 1.0f;
        if (brown < -1.0f) brown = -1.0f;

        lastSamples[channel] = brown;
        return brown;
    }
}
