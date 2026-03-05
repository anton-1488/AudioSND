package org.plovdev.audioengine.generator.strategies.noise;

import java.util.Random;

public class PinkNoise implements NoiseStrategy {
    private static final int NUM_OCTAVES = 8;

    private final Random random;
    private final int[] indices;
    private final float[][] values;
    private int currentIndex;
    private float sum;

    public PinkNoise() {
        this.random = new Random();
        this.indices = new int[NUM_OCTAVES];
        this.values = new float[NUM_OCTAVES][];

        for (int i = 0; i < NUM_OCTAVES; i++) {
            int size = 1 << i;
            indices[i] = 0;
            values[i] = new float[size];
            for (int j = 0; j < size; j++) {
                values[i][j] = (float) (random.nextDouble() * 2.0 - 1.0);
            }
        }
        currentIndex = 0;
        sum = 0;
    }

    @Override
    public float nextSample(int channel) {
        currentIndex++;
        for (int i = 0; i < NUM_OCTAVES; i++) {
            if (currentIndex % (1 << i) == 0) {
                int idx = indices[i];
                sum -= values[i][idx];
                values[i][idx] = (float) (random.nextDouble() * 2.0 - 1.0);
                sum += values[i][idx];
                indices[i] = (idx + 1) % values[i].length;
            }
        }
        return sum / (NUM_OCTAVES + 1) * 2.0f;
    }
}