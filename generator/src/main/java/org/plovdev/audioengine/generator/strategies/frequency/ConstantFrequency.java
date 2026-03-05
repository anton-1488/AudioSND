package org.plovdev.audioengine.generator.strategies.frequency;

/**
 * Linear frequency strategy.
 * <p>
 * Frequency changes linearly from start to end values over the duration.
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public class ConstantFrequency implements FrequencyStrategy {
    private final float[] frequencies;

    public ConstantFrequency(float[] freqs) {
        frequencies = freqs;
    }

    public ConstantFrequency(int channels, float freq) {
        this.frequencies = new float[channels];
        for (int i = 0; i < channels; i++) {
            frequencies[i] = freq;
        }
    }

    /**
     * Returns frequency for given channel at specific sample position.
     *
     * @param channel        channel index (0-based)
     * @param samplePosition current sample from start (0-based)
     * @param totalSamples   total number of samples in the sound
     * @return frequency in Hz for this moment
     */
    @Override
    public float getFrequency(int channel, long samplePosition, long totalSamples) {
        return frequencies[channel];
    }

    /**
     * Returns number of channels this strategy provides.
     *
     * @return channel count
     */
    @Override
    public int getChannels() {
        return frequencies.length;
    }
}