package org.plovdev.audioengine.generator.strategies.modulation;

public interface ModulationStrategy {
    float modulate(long samplePosition, long totalSamples, int channel, float input);
}