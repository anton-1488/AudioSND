package org.plovdev.audioengine.generator.strategies.envelope;

/**
 * Wave envelope strategy that modulates amplitude with a sine wave.
 * <p>
 * This envelope creates periodic amplitude variations (tremolo) by applying
 * </p>
 *
 * @see EnvelopeStrategy
 *
 * @version 1.0
 * @author Anton
 */
public class WaveEnvelope implements EnvelopeStrategy {
    private final float waveRate;
    private final float minAmplitude;
    private final float maxAmplitude;
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