package org.plovdev.audioengine.math;

public class AudioMath {
    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }
}