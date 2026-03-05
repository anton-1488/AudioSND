package org.plovdev.audioengine.generator.strategies.wave;

import org.plovdev.audioengine.math.Functions;

public class TriangleWave implements WaveStrategy {
    /**
     * Generates a single sample value.
     *
     * @param samplePosition current sample from start (0-based)
     * @param frequency      current frequency in Hz
     * @param phase          phase offset in radians
     * @param dutyCycle      duty cycle for square wave (0.0-1.0)
     * @return sample value in range -1.0 to 1.0
     */
    @Override
    public float generate(long samplePosition, float frequency, float phase, float dutyCycle, float sampleRate) {
        float time = samplePosition / sampleRate;
        return Functions.triangle(time, frequency, phase);
    }
}