package org.plovdev.audioengine.generator.strategies.stereo;

public interface StereoStrategy {
    float[] getPan(int channel, float time, float totalTime);
}