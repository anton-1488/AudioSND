package org.plovdev.audioengine.generator.strategies.modulation;

import static org.plovdev.audioengine.math.Functions.TWO_PI;

public class LFOModulation implements ModulationStrategy {
    private final float rate;      // LFO rate in Hz
    private final float depth;      // 0.0-1.0
    private final float sampleRate;

    public LFOModulation(float rate, float depth, float sampleRate) {
        this.rate = rate;
        this.depth = Math.clamp(depth, 0.0f, 1.0f);
        this.sampleRate = sampleRate;
    }

    @Override
    public float modulate(long samplePosition, long totalSamples, int channel, float input) {
        float time = samplePosition / sampleRate;
        float lfo = (float) Math.sin(TWO_PI * rate * time);
        return input * (1.0f + depth * lfo);
    }
}