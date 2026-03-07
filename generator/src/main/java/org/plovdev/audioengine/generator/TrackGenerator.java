package org.plovdev.audioengine.generator;

import org.plovdev.audioengine.generator.config.GenerationConfig;
import org.plovdev.audioengine.generator.strategies.envelope.EnvelopeStrategy;
import org.plovdev.audioengine.generator.strategies.frequency.FrequencyStrategy;
import org.plovdev.audioengine.generator.strategies.modulation.ModulationStrategy;
import org.plovdev.audioengine.generator.strategies.noise.NoiseStrategy;
import org.plovdev.audioengine.generator.strategies.wave.WaveStrategy;
import org.plovdev.audioengine.math.AudioMath;
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
    private GenerationConfig config;
    private TrackFormat trackFormat;

    public TrackGenerator(GenerationConfig config, TrackFormat format) {
        this.config = config;
        trackFormat = format;
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
        ModulationStrategy modulationStrategy = config.getModulationStrategy();
        NoiseStrategy noiseStrategy = config.getNoiseStrategy();

        for (long samplePos = 0; samplePos < totalSamples; samplePos++) {
            float[] channelSamples = new float[channels];

            for (int channel = 0; channel < channels; channel++) {
                float frequency = freqStrategy.getFrequency(channel, samplePos, totalSamples);

                if (modulationStrategy != null) {
                    frequency = modulationStrategy.modulate(samplePos, totalSamples, channel, frequency);
                }
                float sample = waveStrategy.generate(samplePos, frequency, config.getPhase(), sampleRate);
                float amplitude = envStrategy.getAmplitude(samplePos, totalSamples, 1);
                sample *= amplitude;

                if (config.getNoiseLevel() > 0 && noiseStrategy != null) {
                    float noise = noiseStrategy.nextSample(channel);
                    sample = sample * (1 - config.getNoiseLevel()) + noise * config.getNoiseLevel();
                }

                channelSamples[channel] = sample;
            }

            if (channels == 2) {
                float pan = config.getPan();
                float leftGain, rightGain;

                if (pan <= 0) {
                    leftGain = 1.0f;
                    rightGain = 1.0f + pan;
                } else {
                    leftGain = 1.0f - pan;
                    rightGain = 1.0f;
                }

                channelSamples[0] *= leftGain;
                channelSamples[1] *= rightGain;
            }

            for (int channel = 0; channel < channels; channel++) {
                float sample = AudioMath.clamp(channelSamples[channel], -1.0f, 1.0f);
                writeSample(buffer, sample);
            }
        }

        return new Track(buffer, duration, trackFormat, new TrackMetadata());
    }

    private void writeSample(ByteBuffer buffer, float sample) {
        TrackFormat.AudioCodec codec = trackFormat.audioCodec();

        switch (codec) {
            case PCM8:
                buffer.put((byte) (sample * 128));
                break;
            case PCM16:
                buffer.putShort((short) (sample * 32767.0));
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

    public GenerationConfig getConfig() {
        return config;
    }

    public synchronized void setConfig(GenerationConfig config) {
        this.config = config;
    }

    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public synchronized void setTrackFormat(TrackFormat trackFormat) {
        this.trackFormat = trackFormat;
    }
}