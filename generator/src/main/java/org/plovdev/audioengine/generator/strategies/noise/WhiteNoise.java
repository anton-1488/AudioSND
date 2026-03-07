package org.plovdev.audioengine.generator.strategies.noise;

import org.plovdev.audioengine.math.Functions;

/**
 * White noise generator.
 *
 * @author Anton
 * @version 1.0
 */
public class WhiteNoise implements NoiseStrategy {

    /**
     * Returns next white noise sample.
     *
     * @param channel channel index (ignored)
     * @return random value in range [-1.0, 1.0]
     */
    @Override
    public float nextSample(int channel) {
        return Functions.whiteNoise();
    }
}