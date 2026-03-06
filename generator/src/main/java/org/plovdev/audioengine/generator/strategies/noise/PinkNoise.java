package org.plovdev.audioengine.generator.strategies.noise;

import java.util.Random;

public class PinkNoise implements NoiseStrategy {
    private final Random random = new Random();
    private final int numRows = 12; // Чем больше рядов, тем глубже спектр (12 достаточно для аудио)
    private final float[] rows = new float[numRows];
    private float runningSum = 0;
    private int index = 0;

    @Override
    public float nextSample(int channel) {
        int count = Integer.numberOfTrailingZeros(index + 1);

        if (count < numRows) {
            runningSum -= rows[count];
            float newWhite = (random.nextFloat() * 2.0f - 1.0f);
            rows[count] = newWhite;
            runningSum += newWhite;
        }

        index++;
        float white = (random.nextFloat() * 2.0f - 1.0f);
        return (runningSum + white) / (numRows + 1);
    }
}
