package org.plovdev.audioengine.generator.strategies.frequency;

import java.util.Arrays;

/**
 * Frequency strategy that linearly interpolates between start and end frequencies.
 * <p>
 * This strategy creates a smooth linear transition from a starting frequency
 * </p>
 *
 * @version 1.0
 * @author Anton
 * @see FrequencyStrategy
 */
public class LinearFrequency implements FrequencyStrategy {
    private final float[] startFreqs;
    private final float[] endFreqs;

    public LinearFrequency(float[] startFreqs, float[] endFreqs) {
        this.startFreqs = startFreqs;
        this.endFreqs = endFreqs;
    }

    public LinearFrequency(int channels, float start, float end) {
        this.startFreqs = new float[channels];
        this.endFreqs = new float[channels];
        Arrays.fill(startFreqs, start);
        Arrays.fill(endFreqs, end);
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
        float time = (float) samplePosition / totalSamples;
        return startFreqs[channel] + (endFreqs[channel] - startFreqs[channel]) * time;
    }

    /**
     * Returns number of channels this strategy provides.
     *
     * @return channel count
     */
    @Override
    public int getChannels() {
        return startFreqs.length;
    }
}