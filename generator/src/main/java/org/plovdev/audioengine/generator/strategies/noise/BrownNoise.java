package org.plovdev.audioengine.generator.strategies.noise;

import org.plovdev.audioengine.math.Functions;

/**
 * Brown noise generator.
 * <p>
 * Brown noise has a power density decreasing 6dB per octave (-3dB/octave for amplitude).
 * Sounds like a low rumble (waterfall, thunder).
 * </p>
 *
 * @author Anton
 * @version 1.0
 */
public class BrownNoise implements NoiseStrategy {
    private final float[] lastSamples;
    private static final float LEAK_FACTOR = 0.99f;
    private static final float GAIN = 0.05f;

    /**
     * Creates brown noise generator for specified number of channels.
     *
     * @param channels number of audio channels (must be > 0)
     * @throws IllegalArgumentException if channels <= 0
     */
    public BrownNoise(int channels) {
        if (channels <= 0) {
            throw new IllegalArgumentException("Channels must be positive: " + channels);
        }
        this.lastSamples = new float[channels];
    }

    /**
     * Creates brown noise generator for 2 channels.
     */
    public BrownNoise() {
        this(2);
    }


    /**
     * Returns the next noise sample for the specified channel.
     *
     * @param channel channel index (0-based)
     * @return noise sample in range [-1.0, 1.0]
     */
    @Override
    public float nextSample(int channel) {
        float brown = Functions.brownNoise(LEAK_FACTOR, lastSamples[channel], GAIN);

        lastSamples[channel] = brown;
        return brown;
    }
}
