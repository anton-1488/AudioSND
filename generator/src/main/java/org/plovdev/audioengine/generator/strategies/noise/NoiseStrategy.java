package org.plovdev.audioengine.generator.strategies.noise;

public interface NoiseStrategy {
    float nextSample(int channel);
}