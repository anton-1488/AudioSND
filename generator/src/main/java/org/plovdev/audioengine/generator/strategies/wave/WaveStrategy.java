package org.plovdev.audioengine.generator.strategies.wave;

/**
 * Defines the waveform shape for sample generation.
 * <p>
 * Implementations generate different types of waveforms:
 * <ul>
 *   <li>Sine wave - pure tone</li>
 *   <li>Square wave - hollow, reed-like sound</li>
 *   <li>Sawtooth wave - bright, buzzing sound</li>
 *   <li>Triangle wave - soft, flute-like sound</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public interface WaveStrategy {

    /**
     * Generates a single sample value.
     *
     * @param samplePosition current sample from start (0-based)
     * @param frequency current frequency in Hz
     * @param phase phase offset in radians
     * @param sampleRate sample rate
     * @return sample value in range -1.0 to 1.0
     */
    float generate(long samplePosition, float frequency, float phase, float sampleRate);
}