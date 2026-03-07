package org.plovdev.audioengine.generator.strategies.modulation;

import static org.plovdev.audioengine.math.Functions.TWO_PI;

/**
 * LFO modulation strategy.
 *
 * @version 1.0
 * @author Anton
 */
public class LFOModulation implements ModulationStrategy {
    private final float rate;      // LFO rate in Hz
    private final float depth;      // 0.0-1.0
    private final float sampleRate;

    public LFOModulation(float rate, float depth, float sampleRate) {
        this.rate = rate;
        this.depth = Math.clamp(depth, 0.0f, 1.0f);
        this.sampleRate = sampleRate;
    }

    /**
     * Applies modulation to an input sample at a specific point in time.
     * <p>
     * This method is called for every sample in the audio stream. The implementation
     * should apply its modulation effect to the input value and return the modulated result.
     * </p>
     *
     * @param samplePosition the current sample index from the start of the sound (0-based)
     *                       Must be non-negative and typically less than totalSamples
     * @param totalSamples   the total number of samples in the sound
     *                       Must be positive
     * @param channel        the channel index (0 for mono, 0-1 for stereo, etc.)
     *                       Must be non-negative
     * @param input          the original sample value to modulate
     *                       Typically in range [-1.0, 1.0] but can vary by implementation
     * @return the modulated sample value
     */
    @Override
    public float modulate(long samplePosition, long totalSamples, int channel, float input) {
        float time = samplePosition / sampleRate;
        float lfo = (float) Math.sin(TWO_PI * rate * time);
        return input * (1.0f + depth * lfo);
    }
}