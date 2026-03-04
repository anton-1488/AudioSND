package org.plovdev.audioengine.generator;

import org.plovdev.audioengine.generator.config.GeneratorConfig;
import org.plovdev.audioengine.generator.strategies.envelope.EnvelopeStrategy;
import org.plovdev.audioengine.generator.strategies.frequency.FrequencyStrategy;
import org.plovdev.audioengine.generator.strategies.wave.WaveStrategy;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;

/**
 * Audio generator that uses strategies to produce sound.
 * <p>
 * Combines frequency, wave, and envelope strategies to generate audio samples.
 * </p>
 *
 * @version 1.0
 * @author Anton
 */
public class TrackGenerator {
    private GeneratorConfig config;
    private TrackFormat trackFormat;

    public TrackGenerator(GeneratorConfig config, TrackFormat format) {
        this.config = config;
        trackFormat = format;
        validateStrategies();
    }

    private void validateStrategies() {
        if (config.getFrequencyStrategy() == null) {
            throw new IllegalStateException("FrequencyStrategy not set");
        }
        if (config.getWaveStrategy() == null) {
            throw new IllegalStateException("WaveStrategy not set");
        }
        if (config.getEnvelopeStrategy() == null) {
            throw new IllegalStateException("EnvelopeStrategy not set");
        }
    }

    /**
     * Generates audio track of specified duration.
     *
     * @param duration track duration
     * @return generated Track
     */
    public synchronized Track generate(Duration duration) {
        int sampleRate = trackFormat.sampleRate();
        int bitsPerSample = trackFormat.bitDepth();
        int bytesPerSample = trackFormat.bytesPerSample();
        int channels = trackFormat.channels();
        ByteOrder byteOrder = trackFormat.byteOrder();
        long totalSamples = duration.toMillis() * sampleRate / 1000;
        int bufferSize = (int) (totalSamples * channels * bytesPerSample);

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.order(byteOrder);

        FrequencyStrategy freqStrategy = config.getFrequencyStrategy();
        WaveStrategy waveStrategy = config.getWaveStrategy();
        EnvelopeStrategy envStrategy = config.getEnvelopeStrategy();

        for (long samplePos = 0; samplePos < totalSamples; samplePos++) {
            for (int channel = 0; channel < channels; channel++) {
                float frequency = freqStrategy.getFrequency(channel, samplePos, totalSamples);
                float sample = waveStrategy.generate(samplePos, frequency, config.getPhase(), config.getDutyCycle());
                float amplitude = envStrategy.getAmplitude(samplePos, totalSamples, 1.0f);
                sample *= amplitude;
                if (config.getNoiseLevel() > 0 && config.getNoiseStrategy() != null) {
                    float noise = config.getNoiseStrategy().nextSample(channel);
                    sample = sample * (1 - config.getNoiseLevel()) + noise * config.getNoiseLevel();
                }
                sample = clamp(sample, -1.0f, 1.0f);
                writeSample(buffer, sample);
            }
        }

        buffer.flip();
        return new Track(buffer, duration, trackFormat, new TrackMetadata());
    }

    private void writeSample(ByteBuffer buffer, float sample) {
        TrackFormat.AudioCodec codec = trackFormat.audioCodec();

        switch (codec) {
            case PCM8:
                buffer.put((byte) (sample * 128));
                break;
            case PCM16:
                buffer.putShort((short) (sample * 32767));
                break;
            case PCM24:
                int intSample24 = (int) (sample * 8388607);
                if (trackFormat.byteOrder() == ByteOrder.LITTLE_ENDIAN) {
                    buffer.put((byte) (intSample24 & 0xFF));
                    buffer.put((byte) ((intSample24 >> 8) & 0xFF));
                    buffer.put((byte) ((intSample24 >> 16) & 0xFF));
                } else {
                    buffer.put((byte) ((intSample24 >> 16) & 0xFF));
                    buffer.put((byte) ((intSample24 >> 8) & 0xFF));
                    buffer.put((byte) (intSample24 & 0xFF));
                }
                break;
            case PCM32:
                buffer.putLong((long) (sample * 2147483647));
                break;
            case FLOAT32:
                buffer.putFloat(sample);
                break;
            case FLOAT64:
                buffer.putDouble(sample * 2147483647);
                break;
            default:
                throw new UnsupportedOperationException("Bits per sample: " + codec);
        }
    }
    private float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public GeneratorConfig getConfig() {
        return config;
    }

    public synchronized void setConfig(GeneratorConfig config) {
        this.config = config;
    }

    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public synchronized void setTrackFormat(TrackFormat trackFormat) {
        this.trackFormat = trackFormat;
    }
}