package org.plovdev.audioengine.generator.strategies.modulation;

public interface ModulationStrategy {
    float modulate(int channel, float time, float totalTime, float input);
}