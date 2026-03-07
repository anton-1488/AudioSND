package org.plovdev.audioengine.generator.strategies.wave;

import org.plovdev.audioengine.math.Functions;

/**
 * Square wave generation strategy.
 * <p>
 * Generates square wave.
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public class SquareWave implements WaveStrategy {
    private float dutyCycle;
    public SquareWave(float dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public float getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(float dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

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
        return Functions.square(time, frequency, phase, dutyCycle);
    }
}