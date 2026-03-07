package org.plovdev.audioengine.generator.strategies.noise;

import java.util.Random;

/**
 * Pink noise generator.
 * Using Voss-McCartney algorithm.
 *
 * @author Anton
 * @version 1.0
 */
public class PinkNoise implements NoiseStrategy {
    private final Random random;
    private final int numRows = 12;
    private final float[][] rows;
    private final float[] runningSum;
    private final int[] index;

    /**
     * Creates pink noise generator for specified number of channels.
     *
     * @param channels number of audio channels (must be > 0)
     * @param seed random seed for reproducible noise
     * @throws IllegalArgumentException if channels <= 0
     */
    public PinkNoise(int channels, long seed) {
        if (channels <= 0) {
            throw new IllegalArgumentException("Channels must be positive: " + channels);
        }
        this.random = new Random(seed);
        this.rows = new float[channels][numRows];
        this.runningSum = new float[channels];
        this.index = new int[channels];
    }

    /**
     * Creates pink noise generator with random seed.
     *
     * @param channels number of audio channels
     */
    public PinkNoise(int channels) {
        this(channels, System.nanoTime());
    }

    /**
     * Creates pink noise generator for 2 channels (stereo) with random seed.
     */
    public PinkNoise() {
        this(2);
    }

    @Override
    public float nextSample(int channel) {
        int chIndex = index[channel];
        int count = Integer.numberOfTrailingZeros(chIndex + 1);

        if (count < numRows) {
            runningSum[channel] -= rows[channel][count];
            float newWhite = random.nextFloat() * 2.0f - 1.0f;
            rows[channel][count] = newWhite;
            runningSum[channel] += newWhite;
        }

        index[channel] = chIndex + 1;
        float white = random.nextFloat() * 2.0f - 1.0f;
        return (runningSum[channel] + white) / (numRows + 1);
    }
}