package org.plovdev.audioengine.generator.strategies.wave;

import org.plovdev.audioengine.generator.strategies.noise.NoiseStrategy;

public class NoiseWave implements WaveStrategy {
    private final NoiseStrategy strategy;
    public NoiseWave(NoiseStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Generates a single sample value.
     *
     * @param samplePosition current sample from start (0-based)
     * @param frequency      current frequency in Hz
     * @param phase          phase offset in radians
     * @param dutyCycle      duty cycle for square wave (0.0-1.0)
     * @param sampleRate     sample rate
     * @return sample value in range -1.0 to 1.0
     */
    @Override
    public float generate(long samplePosition, float frequency, float phase, float dutyCycle, float sampleRate) {
        return strategy.nextSample(1);
    }
}