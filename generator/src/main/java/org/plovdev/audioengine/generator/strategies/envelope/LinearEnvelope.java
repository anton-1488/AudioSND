package org.plovdev.audioengine.generator.strategies.envelope;

import org.plovdev.audioengine.math.Functions;

/**
 * Linear decay envelope strategy.
 *
 * @see EnvelopeStrategy
 *
 * @version 1.0
 * @author Anton
 */
public class LinearEnvelope implements EnvelopeStrategy {
    private float attack;
    private float release;
    public LinearEnvelope(float attack, float release) {
        this.attack = Math.clamp(attack, 0, 1);
        this.release = Math.clamp(release, 0, 1);
    }

    public float getAttack() {
        return attack;
    }

    public float getRelease() {
        return release;
    }

    public void setAttack(float attack) {
        this.attack = attack;
    }

    public void setRelease(float release) {
        this.release = release;
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
        float time = (float) samplePosition / totalSamples;
        return Functions.linearEnvelope(time, attack, release, baseAmplitude);
    }
}