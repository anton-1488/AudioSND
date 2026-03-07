package org.plovdev.audioengine.generator.strategies.wave;

import org.plovdev.audioengine.math.Functions;

/**
 * Triangle wave generation strategy.
 *
 * @version 1.0
 * @author Anton
 */
public class TriangleWave implements WaveStrategy {
    /**
     * Generates a single sample value.
     *
     * @param samplePosition current sample from start (0-based)
     * @param frequency      current frequency in Hz
     * @param phase          phase offset in radians
     * @return sample value in range -1.0 to 1.0
     */
    @Override
    public float generate(long samplePosition, float frequency, float phase, float sampleRate) {
        float time = samplePosition / sampleRate;
        return Functions.triangle(time, frequency, phase);
    }
}