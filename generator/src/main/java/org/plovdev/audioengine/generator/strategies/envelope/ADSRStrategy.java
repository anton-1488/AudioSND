package org.plovdev.audioengine.generator.strategies.envelope;

import org.plovdev.audioengine.math.Functions;

/**
 * ADSR (Attack-Decay-Sustain-Release) envelope strategy.
 * <p>
 * Classic synthesizer envelope that shapes amplitude over time in four phases:
 * <ul>
 *   <li><b>Attack</b> - amplitude rises from 0 to 1</li>
 *   <li><b>Decay</b> - amplitude drops from 1 to sustain level</li>
 *   <li><b>Sustain</b> - amplitude holds at sustain level</li>
 *   <li><b>Release</b> - amplitude falls from sustain to 0</li>
 * </ul>
 * </p>
 *
 * <p>
 * All durations are specified as fractions of total sound duration (0.0-1.0).
 * For example, attack=0.1 means the first 10% of the sound is the attack phase.
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public class ADSRStrategy implements EnvelopeStrategy {
    private final float attack;
    private final float decay;
    private final float sustain;
    private final float release;

    /**
     * @param attack attack duration (0.0-1.0)
     * @param decay decay duration (0.0-1.0)
     * @param sustain sustain level (0.0-1.0)
     * @param release release duration (0.0-1.0)
     */
    public ADSRStrategy(float attack, float decay, float sustain, float release) {
        this.attack = Math.clamp(attack, 0, 1);
        this.decay = Math.clamp(decay, 0, 1);
        this.sustain = Math.clamp(sustain, 0, 1);
        this.release = Math.clamp(release, 0, 1);
    }

    /**
     * Returns amplitude multiplier for given sample position.
     *
     * @param samplePosition current sample from start (0-based)
     * @param totalSamples   total number of samples in the sound
     * @param baseAmplitude  original amplitude from note/config
     * @return final amplitude for this sample (typically 0.0-1.0)
     */
    @Override
    public float getAmplitude(long samplePosition, long totalSamples, float baseAmplitude) {
        float t = (float) samplePosition / totalSamples;
        return Functions.adsrEnvelope(t, attack, decay, sustain, release, baseAmplitude);
    }
}