package org.plovdev.audioengine.generator.strategies.noise;

/**
 * Strategy for generating noise samples.
 * <p>
 * Implementations provide different types of noise (white, pink, brown, etc.)
 * for each audio channel independently.
 * </p>
 *
 * @author Anton
 * @version 1.0
 */
public interface NoiseStrategy {
    /**
     * Returns the next noise sample for the specified channel.
     *
     * @param channel channel index (0-based)
     * @return noise sample in range [-1.0, 1.0]
     */
    float nextSample(int channel);
}