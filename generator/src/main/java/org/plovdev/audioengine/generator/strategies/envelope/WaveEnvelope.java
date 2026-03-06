package org.plovdev.audioengine.generator.strategies.envelope;

public class WaveEnvelope implements EnvelopeStrategy {
    private final float waveRate;      // частота волн (0.1-2 Гц)
    private final float minAmplitude;   // минимальная громкость (0.0-1.0)
    private final float maxAmplitude;   // максимальная громкость (0.0-1.0)
    private final float sampleRate;

    private static final float TWO_PI = (float) (2.0 * Math.PI);

    public WaveEnvelope(float waveRate, float minAmplitude, float maxAmplitude, float sampleRate) {
        this.waveRate = waveRate;
        this.minAmplitude = Math.clamp(minAmplitude, 0, 1);
        this.maxAmplitude = Math.clamp(maxAmplitude, 0, 1);
        this.sampleRate = sampleRate;
    }

    @Override
    public float getAmplitude(long samplePosition, long totalSamples, float baseAmplitude) {
        float time = samplePosition / sampleRate;
        float wave = (float) (Math.sin(TWO_PI * waveRate * time) + 1.0) / 2.0f;
        float envelope = minAmplitude + wave * (maxAmplitude - minAmplitude);

        return baseAmplitude * envelope;
    }
}