package org.plovdev.audioengine.generator.strategies.frequency;

/**
 * Defines how frequency changes over time during sound generation.
 * <p>
 * Implementations determine the instantaneous frequency for each channel
 * at each sample position. This allows for:
 * <ul>
 *   <li>Constant tones (same frequency throughout)</li>
 *   <li>Frequency sweeps (linear or exponential)</li>
 *   <li>LFO modulation (vibrato)</li>
 *   <li>Musical notes and melodies</li>
 *   <li>Random frequency variations</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public interface FrequencyStrategy {
    /**
     * Returns frequency for given channel at specific sample position.
     *
     * @param channel channel index (0-based)
     * @param samplePosition current sample from start (0-based)
     * @param totalSamples total number of samples in the sound
     * @return frequency in Hz for this moment
     */
    float getFrequency(int channel, long samplePosition, long totalSamples);

    /**
     * Returns number of channels this strategy provides.
     *
     * @return channel count
     */
    int getChannels();
}