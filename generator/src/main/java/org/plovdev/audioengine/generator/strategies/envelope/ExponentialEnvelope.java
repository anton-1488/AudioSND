package org.plovdev.audioengine.generator.strategies.envelope;

import org.plovdev.audioengine.math.Functions;

public class ExponentialEnvelope implements EnvelopeStrategy {
    private float decay;

    public ExponentialEnvelope(float decay) {
        this.decay = decay;
    }

    public float getDecay() {
        return decay;
    }

    public void setDecay(float decay) {
        this.decay = decay;
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
        float time = (float) samplePosition / totalSamples;
        return Functions.expEnvelope(time, decay, baseAmplitude);
    }
}