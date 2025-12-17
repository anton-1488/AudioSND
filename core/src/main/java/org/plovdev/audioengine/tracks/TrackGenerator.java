package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

public class TrackGenerator {

    public enum WaveType {
        SINE,
        SQUARE,
        SAWTOOTH,
        TRIANGLE,
        NOISE,
        IMPULSE,
        SILENCE,
        CHIRP,
        SWEEP
    }

    public static class GeneratorConfig {
        private final double[] frequencies;      // Частота для каждого канала
        private final double[] amplitudes;       // Амплитуда для каждого канала (0.0-1.0)
        private final WaveType waveType;
        private final double phase;
        private final double dutyCycle;          // Скоажность для прямоугольной волны
        private final double noiseLevel;         // Уровень шума (0.0-1.0)

        // Для параметрической генерации
        private final boolean frequencySweep;
        private final double startFrequency;
        private final double endFrequency;
        private final boolean amplitudeEnvelope;
        private final EnvelopeType envelopeType;

        public enum EnvelopeType {
            NONE,
            ADSR,      // Attack-Decay-Sustain-Release
            LINEAR,
            EXPONENTIAL,
            HANN,      // Оконная функция Ханна
            HAMMING    // Оконная функция Хэмминга
        }

        private GeneratorConfig(Builder builder) {
            this.frequencies = builder.frequencies;
            this.amplitudes = builder.amplitudes;
            this.waveType = builder.waveType;
            this.phase = builder.phase;
            this.dutyCycle = builder.dutyCycle;
            this.noiseLevel = builder.noiseLevel;
            this.frequencySweep = builder.frequencySweep;
            this.startFrequency = builder.startFrequency;
            this.endFrequency = builder.endFrequency;
            this.amplitudeEnvelope = builder.amplitudeEnvelope;
            this.envelopeType = builder.envelopeType;
        }

        public static class Builder {
            private double[] frequencies = {440.0};
            private double[] amplitudes = {0.5};
            private WaveType waveType = WaveType.SINE;
            private double phase = 0.0;
            private double dutyCycle = 0.5;
            private double noiseLevel = 0.0;
            private boolean frequencySweep = false;
            private double startFrequency = 440.0;
            private double endFrequency = 880.0;
            private boolean amplitudeEnvelope = false;
            private EnvelopeType envelopeType = EnvelopeType.NONE;

            public Builder channels(int numChannels) {
                this.frequencies = new double[numChannels];
                this.amplitudes = new double[numChannels];
                Arrays.fill(this.frequencies, 440.0);
                Arrays.fill(this.amplitudes, 0.5);
                return this;
            }

            public Builder frequency(double frequency) {
                Arrays.fill(this.frequencies, frequency);
                return this;
            }

            public Builder frequency(double[] frequencies) {
                this.frequencies = frequencies.clone();
                return this;
            }

            public Builder frequency(int channel, double frequency) {
                if (channel >= 0 && channel < this.frequencies.length) {
                    this.frequencies[channel] = frequency;
                }
                return this;
            }

            public Builder amplitude(double amplitude) {
                double amp = Math.max(0.0, Math.min(1.0, amplitude));
                Arrays.fill(this.amplitudes, amp);
                return this;
            }

            public Builder amplitude(double[] amplitudes) {
                this.amplitudes = new double[amplitudes.length];
                for (int i = 0; i < amplitudes.length; i++) {
                    this.amplitudes[i] = Math.max(0.0, Math.min(1.0, amplitudes[i]));
                }
                return this;
            }

            public Builder amplitude(int channel, double amplitude) {
                if (channel >= 0 && channel < this.amplitudes.length) {
                    this.amplitudes[channel] = Math.max(0.0, Math.min(1.0, amplitude));
                }
                return this;
            }

            public Builder waveType(WaveType waveType) {
                this.waveType = waveType;
                return this;
            }

            public Builder phase(double phase) {
                this.phase = phase;
                return this;
            }

            public Builder dutyCycle(double dutyCycle) {
                this.dutyCycle = Math.max(0.0, Math.min(1.0, dutyCycle));
                return this;
            }

            public Builder noiseLevel(double noiseLevel) {
                this.noiseLevel = Math.max(0.0, Math.min(1.0, noiseLevel));
                return this;
            }

            public Builder frequencySweep(double start, double end) {
                this.frequencySweep = true;
                this.startFrequency = start;
                this.endFrequency = end;
                return this;
            }

            public Builder amplitudeEnvelope(EnvelopeType type) {
                this.amplitudeEnvelope = true;
                this.envelopeType = type;
                return this;
            }

            public GeneratorConfig build() {
                return new GeneratorConfig(this);
            }
        }
    }

    /**
     * Генерирует трек на основе формата и конфигурации
     */
    public static Track generate(Duration duration, TrackFormat format, GeneratorConfig config) {
        int sampleRate = format.sampleRate();
        int channels = format.channels();
        int bitsPerSample = format.bitsPerSample();
        boolean signed = false;
        ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

        // Автоматически корректируем конфиг под количество каналов
        if (config.frequencies.length != channels) {
            double[] newFreqs = new double[channels];
            double[] newAmps = new double[channels];

            for (int i = 0; i < channels; i++) {
                newFreqs[i] = config.frequencies[Math.min(i, config.frequencies.length - 1)];
                newAmps[i] = config.amplitudes[Math.min(i, config.amplitudes.length - 1)];
            }

            config = new GeneratorConfig.Builder()
                    .waveType(config.waveType)
                    .frequency(newFreqs)
                    .amplitude(newAmps)
                    .phase(config.phase)
                    .dutyCycle(config.dutyCycle)
                    .noiseLevel(config.noiseLevel)
                    .build();
        }

        int numSamples = (int) (duration.toMillis() * sampleRate / 1000);
        int bytesPerSample = bitsPerSample / 8;
        int frameSize = channels * bytesPerSample;
        int bufferSize = numSamples * frameSize;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.order(byteOrder);

        Random random = new Random();
        double totalTime = duration.toMillis() / 1000.0;

        // Генерируем сэмплы
        for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
            double time = sampleIndex / (double) sampleRate;
            double normalizedTime = time / totalTime;

            // Применяем envelope к амплитуде
            double envelopeFactor = config.amplitudeEnvelope ?
                    calculateEnvelope(normalizedTime, config.envelopeType) : 1.0;

            for (int channel = 0; channel < channels; channel++) {
                // Рассчитываем частоту с учетом sweep
                double frequency = config.frequencySweep ?
                        config.startFrequency + (config.endFrequency - config.startFrequency) * normalizedTime :
                        config.frequencies[channel];

                double sample = generateSampleForChannel(
                        sampleIndex, time, frequency, channel, config, random
                );

                // Применяем амплитуду и envelope
                sample *= config.amplitudes[channel] * envelopeFactor;


                // Ограничиваем и квантуем
                sample = Math.tanh(sample);

                // Записываем в буфер в соответствии с форматом
                writeSampleToBuffer(buffer, sample, bitsPerSample, signed);
            }
        }

        buffer.flip();

        return new Track(buffer, duration, format, new TrackMetadata());
    }

    private static double generateSampleForChannel(int sampleIndex, double time,
                                                   double frequency, int channel,
                                                   GeneratorConfig config, Random random) {
        double angularFreq = 2.0 * Math.PI * frequency;
        double phase = config.phase + channel * Math.PI / 2.0; // Сдвиг фазы для стерео

        final double v = (angularFreq * time + phase) % (2.0 * Math.PI);
        return switch (config.waveType) {
            case SINE -> Math.sin(angularFreq * time + phase);
            case SQUARE -> v < (2.0 * Math.PI * config.dutyCycle) ? 1.0 : -1.0;
            case SAWTOOTH -> (v / (Math.PI)) - 1.0;
            case TRIANGLE -> v < Math.PI ?
                    (2.0 * v / Math.PI) - 1.0 :
                    (2.0 * (2.0 * Math.PI - v) / Math.PI) - 1.0;
            case NOISE -> random.nextDouble() * 2.0 - 1.0;
            case IMPULSE -> (sampleIndex == 0) ? 1.0 : 0.0;
            case CHIRP -> {
                // Линейный чирп: f(t) = f0 + (f1 - f0) * t/T
                double chirpFreq = config.startFrequency +
                        (config.endFrequency - config.startFrequency) * time;
                yield Math.sin(2.0 * Math.PI * chirpFreq * time + phase);
            }
            case SWEEP -> {
                // Экспоненциальный свип
                double sweepFreq = config.startFrequency *
                        Math.pow(config.endFrequency / config.startFrequency, time);
                yield Math.sin(2.0 * Math.PI * sweepFreq * time + phase);
            }
            default -> 0.0;
        };
    }

    private static double calculateEnvelope(double normalizedTime, GeneratorConfig.EnvelopeType type) {
        switch (type) {
            case ADSR:
                // Простая ADSR оболочка
                double attack = 0.1;
                double decay = 0.2;
                double sustain = 0.7;
                double release = 0.2;

                if (normalizedTime < attack) {
                    return normalizedTime / attack; // Attack
                } else if (normalizedTime < attack + decay) {
                    double decayTime = normalizedTime - attack;
                    return 1.0 - (decayTime / decay) * (1.0 - sustain); // Decay
                } else if (normalizedTime < 1.0 - release) {
                    return sustain; // Sustain
                } else {
                    double releaseTime = normalizedTime - (1.0 - release);
                    return sustain * (1.0 - releaseTime / release); // Release
                }

            case LINEAR:
                return normalizedTime < 0.5 ?
                        normalizedTime * 2.0 :
                        (1.0 - normalizedTime) * 2.0;

            case EXPONENTIAL:
                return Math.exp(-5.0 * Math.abs(normalizedTime - 0.5));

            case HANN:
                return 0.5 * (1.0 - Math.cos(2.0 * Math.PI * normalizedTime));

            case HAMMING:
                return 0.54 - 0.46 * Math.cos(2.0 * Math.PI * normalizedTime);

            case NONE:
            default:
                return 1.0;
        }
    }

    private static void writeSampleToBuffer(ByteBuffer buffer, double sample,
                                            int bitsPerSample, boolean signed) {
        switch (bitsPerSample) {
            case 8:
                byte value1;
                if (signed) {
                    value1 = (byte) (sample * 127.0);
                } else {
                    value1 = (byte) ((sample + 1.0) * 127.5);
                }
                buffer.put(value1);
                break;

            case 16:
                if (signed) {
                    short value2 = (short) (sample * 32767.0);
                    buffer.putShort(value2);
                } else {
                    int value3 = (int) ((sample + 1.0) * 32767.5);
                    buffer.putShort((short) value3);
                }
                break;

            case 24:
                // 24-bit в 3 байтах
                double scaled = signed ?
                        sample * 8388607.0 : // 2^23 - 1
                        (sample + 1.0) * 8388607.5;
                int intValue = (int) scaled;

                if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
                    buffer.put((byte) (intValue & 0xFF));
                    buffer.put((byte) ((intValue >> 8) & 0xFF));
                    buffer.put((byte) ((intValue >> 16) & 0xFF));
                } else {
                    buffer.put((byte) ((intValue >> 16) & 0xFF));
                    buffer.put((byte) ((intValue >> 8) & 0xFF));
                    buffer.put((byte) (intValue & 0xFF));
                }
                break;

            case 32:
                if (signed) {
                    int value = (int) (sample * 2147483647.0);
                    buffer.putInt(value);
                } else {
                    long longValue = (long) ((sample + 1.0) * 2147483647.5);
                    buffer.putInt((int) longValue);
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported bits per sample: " + bitsPerSample);
        }
    }

    /**
     * Создание тестовых треков
     */
    public static Track createTestTone(TrackFormat format, Duration duration) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(440.0) // Ля первой октавы
                .amplitude(0.01)
                .waveType(WaveType.SINE)
                .build();

        return generate(duration, format, config);
    }

    public static Track createWhiteNoise(TrackFormat format, Duration duration) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .amplitude(0.1)
                .waveType(WaveType.NOISE)
                .build();

        return generate(duration, format, config);
    }

    public static Track createSilence(TrackFormat format, Duration duration) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .amplitude(0.0)
                .waveType(WaveType.SILENCE)
                .build();

        return generate(duration, format, config);
    }

    /**
     * Создание стерео теста с разными частотами в каналах
     */
    public static Track createStereoTest(TrackFormat format, Duration duration) {
        if (format.channels() != 2) {
            throw new IllegalArgumentException("Format must be stereo (2 channels)");
        }

        double[] frequencies = {440.0, 440.0}; // Ля и До# следующей октавы
        double[] amplitudes = {0.3, 0.3};

        GeneratorConfig config = new GeneratorConfig.Builder()
                .frequency(frequencies)
                .amplitude(amplitudes)
                .waveType(WaveType.SINE)
                .build();

        return generate(duration, format, config);
    }

    /**
     * Создание чирпа (частотной развертки)
     */
    public static Track createChirp(TrackFormat format, Duration duration,
                                    double startFreq, double endFreq) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(startFreq)
                .amplitude(0.3)
                .waveType(WaveType.CHIRP)
                .frequencySweep(startFreq, endFreq)
                .build();

        return generate(duration, format, config);
    }
}