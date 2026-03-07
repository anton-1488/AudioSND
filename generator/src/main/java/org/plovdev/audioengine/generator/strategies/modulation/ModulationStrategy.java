package org.plovdev.audioengine.generator.strategies.modulation;


/**
 * Strategy interface for audio modulation effects.
 *
 * @version 1.0
 * @author Anton
 */
public interface ModulationStrategy {
    /**
     * Applies modulation to an input sample at a specific point in time.
     * <p>
     * This method is called for every sample in the audio stream. The implementation
     * should apply its modulation effect to the input value and return the modulated result.
     * </p>
     *
     * @param samplePosition the current sample index from the start of the sound (0-based)
     *                       Must be non-negative and typically less than totalSamples
     * @param totalSamples   the total number of samples in the sound
     *                       Must be positive
     * @param channel        the channel index (0 for mono, 0-1 for stereo, etc.)
     *                       Must be non-negative
     * @param input          the original sample value to modulate
     *                       Typically in range [-1.0, 1.0] but can vary by implementation
     * @return the modulated sample value
     */
    float modulate(long samplePosition, long totalSamples, int channel, float input);
}