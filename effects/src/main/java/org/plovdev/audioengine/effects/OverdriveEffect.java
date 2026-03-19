package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.format.TrackFormat;

import java.nio.ByteBuffer;

public class OverdriveEffect implements AudioEffect {
    public enum Type {
        SOFT_CLIPPING, HARD_CLIPPING, TUBE_SIMULATION, FUZZ, WAVESHAPING
    }

    private TrackFormat format;
    private Type type;
    private float drive;
    private float tone;
    private float level;
    private float mix;
    private ToneFilter toneFilter;
    private float previousSample;
    private float maxSampleValue;

    public OverdriveEffect() {
        this(Type.FUZZ, 0.5f, 0.8f, 0.5f, 0.5f);
    }

    public OverdriveEffect(Type type, float drive, float tone, float level, float mix) {
        this.type = type;
        setDrive(drive);
        setTone(tone);
        setLevel(level);
        setMix(mix);
        this.previousSample = 0.0f;
    }

    @Override
    public void setup(TrackFormat format) {
        this.format = format;
        this.maxSampleValue = calculateMaxSampleValue(format);

        if (toneFilter == null) {
            this.toneFilter = new ToneFilter((float) format.sampleRate(), tone);
        } else {
            toneFilter.setSampleRate((float) format.sampleRate());
            toneFilter.setCutoff(tone);
        }
    }

    @Override
    public ByteBuffer process(ByteBuffer source) {
        if (format == null) {
            throw new IllegalStateException("Effect not initialized. Call setup() first.");
        }

        source.order(format.byteOrder());
        source.rewind();

        int bytesPerSample = format.bytesPerSample() / format.channels();
        int numSamples = source.remaining() / format.bytesPerSample();

        ByteBuffer processed = ByteBuffer.allocateDirect(source.remaining());
        processed.order(format.byteOrder());

        for (int i = 0; i < numSamples; i++) {
            for (int channel = 0; channel < format.channels(); channel++) {
                float sample = readSample(source, bytesPerSample);
                float processedSample = processSample(sample);
                writeSample(processed, processedSample, bytesPerSample);
            }
        }

        processed.flip();
        return processed;
    }

    private float processSample(float sample) {
        float distorted = applyDistortion(sample);
        distorted = toneFilter.process(distorted);
        float mixed = sample * (1 - mix) + distorted * mix;
        mixed *= level;
        return denormalizeSample(mixed);
    }

    private float applyDistortion(float sample) {
        float amplified = sample * (1 + drive * 10);
        return switch (type) {
            case SOFT_CLIPPING -> softClipping(amplified);
            case HARD_CLIPPING -> hardClipping(amplified);
            case TUBE_SIMULATION -> tubeSimulation(amplified);
            case FUZZ -> fuzzDistortion(amplified);
            case WAVESHAPING -> waveShaping(amplified);
        };
    }

    private float softClipping(float input) {
        final float threshold = 0.8f;
        final float knee = 0.1f;
        final float tpk = threshold + knee;
        final float tmk = threshold - knee;

        if (input > tpk) {
            float diff = input - tpk;
            return threshold + diff / (1 + diff * diff);
        } else if (input > tmk) {
            float diff = input - threshold;
            float ratio = diff * diff / (3 * knee * knee);
            return threshold + diff * (1 - ratio);
        }
        return input;
    }

    private float hardClipping(float input) {
        final float threshold = 0.7f;
        return clamp(input, -threshold, threshold);
    }

    private float tubeSimulation(float input) {
        final float gain = 2.0f;
        final float tanhGain = (float) Math.tanh(gain);
        float shaped = (float) Math.tanh(input * gain) / tanhGain;

        return shaped > 0 ? shaped * 0.95f : shaped;
    }

    private float fuzzDistortion(float input) {
        float distorted;
        if (input > 0.1f) {
            distorted = 1.0f;
        } else if (input < -0.1f) {
            distorted = -1.0f;
        } else {
            distorted = input * 10.0f;
        }
        distorted = 0.7f * distorted + 0.3f * previousSample;
        previousSample = distorted;

        return distorted;
    }

    private float waveShaping(float input) {
        float x = clamp(input, -1.0f, 1.0f);
        float x2 = x * x;
        float x3 = x2 * x;

        return clamp(x - x3 * 0.3333333f + 0.2f * (x2 - 0.5f), -1.0f, 1.0f);
    }

    private float readSample(ByteBuffer buffer, int bytesPerSample) {
        return switch (bytesPerSample) {
            case 1 -> format.signed() ?
                    buffer.get() / 127.0f :
                    (buffer.get() - 128) / 128.0f;
            case 2 -> buffer.getShort() / 32767.0f;
            case 3 -> {
                byte b1 = buffer.get();
                byte b2 = buffer.get();
                byte b3 = buffer.get();
                int sample24 = ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF);
                yield format.signed() ?
                        sample24 / 8388607.0f :
                        (sample24 - 8388608) / 8388608.0f;
            }
            case 4 -> format.audioCodec() == TrackFormat.AudioCodec.FLOAT32 ?
                    buffer.getFloat() :
                    buffer.getInt() / 2147483647.0f;
            case 8 -> (float) buffer.getDouble();
            default -> throw new IllegalArgumentException("Unsupported sample size: " + bytesPerSample);
        };
    }

    private void writeSample(ByteBuffer buffer, float sample, int bytesPerSample) {
        sample = clamp(sample, -1.0f, 1.0f);

        switch (bytesPerSample) {
            case 1:
                buffer.put((byte) (format.signed() ?
                        sample * 127.0f :
                        sample * 128.0f + 128.0f));
                break;
            case 2:
                buffer.putShort((short) (sample * 32767.0f));
                break;
            case 3:
                int sample24 = (int) (sample * (format.signed() ? 8388607.0f : 8388608.0f));
                if (!format.signed()) sample24 += 8388608;
                buffer.put((byte) sample24);
                buffer.put((byte) (sample24 >> 8));
                buffer.put((byte) (sample24 >> 16));
                break;
            case 4:
                if (format.audioCodec() == TrackFormat.AudioCodec.FLOAT32) {
                    buffer.putFloat(sample);
                } else {
                    buffer.putInt((int) (sample * 2147483647.0f));
                }
                break;
            case 8:
                buffer.putDouble(sample);
                break;
        }
    }

    private float denormalizeSample(float sample) {
        // Для float форматов оставляем как есть
        return sample; // readSample уже нормализовал, возвращаем
    }

    private float calculateMaxSampleValue(TrackFormat format) {
        if (format.audioCodec() == TrackFormat.AudioCodec.FLOAT32 ||
                format.audioCodec() == TrackFormat.AudioCodec.FLOAT64) {
            return 1.0f;
        }

        int bits = format.bitDepth();
        if (format.signed()) {
            return (float) Math.pow(2, bits - 1) - 1;
        } else {
            return (float) Math.pow(2, bits) - 1;
        }
    }

    private float clamp(float value, float min, float max) {
        return value < min ? min : (Math.min(value, max));
    }

    public void setDrive(float drive) {
        this.drive = clamp(drive, 0.0f, 2.0f);
    }

    public void setTone(float tone) {
        this.tone = clamp(tone, 0.0f, 1.0f);
        if (toneFilter != null) toneFilter.setCutoff(tone);
    }

    public void setLevel(float level) {
        this.level = clamp(level, 0.0f, 2.0f);
    }

    public void setMix(float mix) {
        this.mix = clamp(mix, 0.0f, 1.0f);
    }


    public TrackFormat getFormat() {
        return format;
    }

    public void setFormat(TrackFormat format) {
        this.format = format;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public float getDrive() {
        return drive;
    }

    public float getTone() {
        return tone;
    }

    public float getLevel() {
        return level;
    }

    public float getMix() {
        return mix;
    }

    public float getPreviousSample() {
        return previousSample;
    }

    public void setPreviousSample(float previousSample) {
        this.previousSample = previousSample;
    }

    public float getMaxSampleValue() {
        return maxSampleValue;
    }

    public void setMaxSampleValue(float maxSampleValue) {
        this.maxSampleValue = maxSampleValue;
    }

    private static class ToneFilter {
        private float sampleRate;
        private float cutoff;
        private float alpha;
        private float lowPassOutput;
        private float highPassOutput;
        private boolean needsUpdate = true;

        public ToneFilter(float sampleRate, float cutoff) {
            this.sampleRate = sampleRate;
            setCutoff(cutoff);
            reset();
        }

        public void setSampleRate(float sampleRate) {
            this.sampleRate = sampleRate;
            this.needsUpdate = true;
        }

        public void setCutoff(float cutoff) {
            this.cutoff = 200.0f + cutoff * 7800.0f;
            this.needsUpdate = true;
        }

        public float process(float input) {
            if (needsUpdate) {
                updateCoefficients();
            }

            lowPassOutput = alpha * input + (1 - alpha) * lowPassOutput;
            highPassOutput = input - lowPassOutput;

            float mixFactor = (cutoff - 200.0f) / 7800.0f;
            return lowPassOutput * (1 - mixFactor) + highPassOutput * mixFactor;
        }

        private void updateCoefficients() {
            float rc = 1.0f / (2.0f * (float) Math.PI * cutoff);
            float dt = 1.0f / sampleRate;
            alpha = dt / (rc + dt);
            needsUpdate = false;
        }

        public void reset() {
            lowPassOutput = 0.0f;
            highPassOutput = 0.0f;
        }
    }
}