package org.plovdev.audioengine.generator.strategies.wave;

import org.plovdev.audioengine.math.Functions;

/**
 * Sawtooth wave generation strategy.
 *
 * @version 1.0
 * @author Anton
 */
public class SawtoothWave implements WaveStrategy {
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
        return Functions.sawtooth(time, frequency, phase);
    }
}