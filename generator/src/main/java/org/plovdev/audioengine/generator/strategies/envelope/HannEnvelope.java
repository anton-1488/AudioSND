package org.plovdev.audioengine.generator.strategies.envelope;

import org.plovdev.audioengine.math.Functions;

public class HannEnvelope implements EnvelopeStrategy {

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
        return Functions.hannEnvelope(time, baseAmplitude);
    }
}