package org.plovdev.audioengine.generator.strategies.envelope;

public class ConstantEnvelope implements EnvelopeStrategy {
    private float amplitude;

    public ConstantEnvelope(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    /**
     * Returns amplitude multiplier for given sample position.
     *
     * @param samplePosition current sample from start (0-based)
     * @param totalSamples   total number of samples in the sound
     * @param baseAmplitude  original amplitude from note/config
     * @return final amplitude for this sample (typically 0.0-1.0)
     */
    @Override
    public float getAmplitude(long samplePosition, long totalSamples, float baseAmplitude) {
        return amplitude;
    }
}