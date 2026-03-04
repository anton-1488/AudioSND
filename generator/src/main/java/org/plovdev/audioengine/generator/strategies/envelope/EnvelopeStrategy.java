package org.plovdev.audioengine.generator.strategies.envelope;

/**
 * Defines how amplitude changes over time during sound generation.
 * <p>
 * Implementations modify the base amplitude based on the current sample position
 * to create effects like fade in/out, ADSR, tremolo, or window functions.
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
@FunctionalInterface
public interface EnvelopeStrategy {
    /**
     * Returns amplitude multiplier for given sample position.
     *
     * @param samplePosition current sample from start (0-based)
     * @param totalSamples total number of samples in the sound
     * @param baseAmplitude original amplitude from note/config
     * @return final amplitude for this sample (typically 0.0-1.0)
     */
    float getAmplitude(long samplePosition, long totalSamples, float baseAmplitude);
}