package org.plovdev.audioengine.generator.strategies.wave;

/**
 * Sine wave generation strategy.
 * <p>
 * Generates pure sine wave: sin(2π * frequency * time + phase)
 * where time = samplePosition / sampleRate
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public class SineWave implements WaveStrategy {
    private static final float TWO_PI = (float) (2.0 * Math.PI);
    private final float sampleRate;

    public SineWave(float sampleRate) {
        this.sampleRate = sampleRate;
    }

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
    public float generate(long samplePosition, float frequency, float phase, float dutyCycle) {
        float time = samplePosition / sampleRate;
        return (float) Math.sin(TWO_PI * frequency * time + phase);
    }
}