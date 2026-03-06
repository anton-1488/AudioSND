package org.plovdev.audioengine.math;

import java.util.concurrent.ThreadLocalRandom;

public class Functions {
    public static final float PI = 3.1415927f;
    public static final float TWO_PI = 6.2831855f;

    private Functions() {}

    public static float sine(float time, float frequency, float phase) {
        return (float) Math.sin(TWO_PI * frequency * time + phase);
    }
    public static float square(float time, float frequency, float phase, float dutyCycle) {
        float t = (time * frequency + phase / TWO_PI) % 1;
        return t < dutyCycle ? 1 : -1;
    }
    public static float sawtooth(float time, float frequency, float phase) {
        float t = (time * frequency + phase / TWO_PI) % 1.0f;
        return 2.0f * t - 1.0f;
    }

    public static float triangle(float time, float frequency, float phase) {
        float t = (time * frequency + phase / TWO_PI) % 1.0f;
        return 2.0f * Math.abs(2.0f * t - 1.0f) - 1.0f;
    }

    public static float noise() {
        return (float) (Math.random() * 2.0 - 1.0);
    }

    public static float linearEnvelope(float time, float attack, float release, float base) {
        float amplitude;

        if (time < attack) {
            amplitude = time / attack;
        } else if (time > 1 - release) {
            amplitude = (1 - time) / release;
        } else {
            amplitude = 1;
        }

        return base * amplitude;
    }
    public static float adsrEnvelope(float t, float attack, float decay, float sustain, float release, float base) {
        float amp;

        if (t < attack) {
            amp = t / attack;
        } else if (t < attack + decay) {
            float decayTime = (t - attack) / decay;
            amp = 1 - (1 - sustain) * decayTime;
        } else if (t < 1 - release) {
            amp = sustain;
        } else {
            float releaseTime = (t - (1 - release)) / release;
            amp = sustain * (1 - releaseTime);
        }

        return base * amp;
    }
    public static float expEnvelope(float time, float decay, float base) {
        return base * (float) Math.exp(-decay * time);
    }

    public static float hannEnvelope(float time, float base) {
        float window = 0.5f * (1 - (float) Math.cos(TWO_PI * time));
        return base * window;
    }

    public static float whiteNoise() {
        return (float) (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0);
    }
}