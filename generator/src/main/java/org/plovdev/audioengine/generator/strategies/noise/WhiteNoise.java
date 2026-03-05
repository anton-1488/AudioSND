package org.plovdev.audioengine.generator.strategies.noise;

import org.plovdev.audioengine.math.Functions;

public class WhiteNoise implements NoiseStrategy {
    @Override
    public float nextSample(int channel) {
        return Functions.whiteNoise();
    }
}