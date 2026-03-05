package org.plovdev.audioengine.generator.strategies.noise;

import org.plovdev.audioengine.math.Functions;

public class BrownNoise implements NoiseStrategy {
    private float lastSample;

    public BrownNoise() {
        this.lastSample = 0.0f;
    }

    @Override
    public float nextSample(int channel) {
        float sample = lastSample + Functions.whiteNoise() * 0.02f;

        if (sample > 1.0f) sample = 1.0f;
        if (sample < -1.0f) sample = -1.0f;

        lastSample = sample;
        return sample;
    }
}
