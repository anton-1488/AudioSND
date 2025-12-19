package org.plovdev.audioengine.generator;

import org.plovdev.audioengine.exceptions.GenerationException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Random;

public class TrackGeneratorHelper {
    private static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
    private static final Logger log = LoggerFactory.getLogger(TrackGeneratorHelper.class);

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
        double[] frequencies = config.getFrequencies();
        int freqsLength = frequencies.length;

        double[] amplitudes = config.getAmplitudes();
        int amplLength = amplitudes.length;

        if (freqsLength != channels) {
            double[] newFreqs = new double[channels];
            double[] newAmps = new double[channels];

            for (int i = 0; i < channels; i++) {
                newFreqs[i] = frequencies[Math.min(i, freqsLength - 1)];
                newAmps[i] = amplitudes[Math.min(i, amplLength - 1)];
            }

            config = new GeneratorConfig.Builder()
                    .waveType(config.getWaveType())
                    .frequency(newFreqs)
                    .amplitude(newAmps)
                    .phase(config.getPhase())
                    .dutyCycle(config.getDutyCycle())
                    .noiseLevel(config.getNoiseLevel())
                    .build();
        }

        int numSamples = (int) (duration.toMillis() * sampleRate / 1000);
        int bytesPerSample = bitsPerSample / 8;
        int frameSize = channels * bytesPerSample;
        int bufferSize = numSamples * frameSize;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.order(byteOrder);

        long millis = duration.toMillis();
        if (millis <= 0) {
            throw new GenerationException("Duration can't be zero.");
        }

        double totalTime = millis / 1000.0;

        // Генерируем сэмплы
        for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
            double time = sampleIndex / (double) sampleRate;
            double normalizedTime = time / totalTime;

            // Применяем envelope к амплитуде
            double envelopeFactor = config.isAmplitudeEnvelope() ? calculateEnvelope(normalizedTime, config.getEnvelopeType()) : 1.0;

            for (int channel = 0; channel < channels; channel++) {
                // Рассчитываем частоту с учетом sweep
                double frequency = config.isFrequencySweep() ?
                        config.getStartFrequency() + (config.getEndFrequency() - config.getStartFrequency()) * normalizedTime :
                        frequencies[channel];

                double sample = generateSampleForChannel(sampleIndex, time, frequency, channel, config);

                // Применяем амплитуду и envelope
                sample *= amplitudes[channel] * envelopeFactor;


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
                                                   GeneratorConfig config) {
        double angularFreq = 2.0 * Math.PI * frequency;
        double phase = config.getPhase() + channel * Math.PI / 2.0; // Сдвиг фазы для стерео

        final double v = (angularFreq * time + phase) % (2.0 * Math.PI);
        return switch (config.getWaveType()) {
            case SINE -> Math.sin(angularFreq * time + phase);
            case SQUARE -> v < (2.0 * Math.PI * config.getDutyCycle()) ? 1.0 : -1.0;
            case SAWTOOTH -> (v / (Math.PI)) - 1.0;
            case TRIANGLE -> v < Math.PI ?
                    (2.0 * v / Math.PI) - 1.0 :
                    (2.0 * (2.0 * Math.PI - v) / Math.PI) - 1.0;
            case NOISE -> random.get().nextDouble() * 2.0 - 1.0;
            case IMPULSE -> (sampleIndex == 0) ? 1.0 : 0.0;
            case CHIRP -> {
                // Линейный чирп: f(t) = f0 + (f1 - f0) * t/T
                double chirpFreq = config.getStartFrequency() +
                        (config.getEndFrequency() - config.getStartFrequency()) * time;
                yield Math.sin(2.0 * Math.PI * chirpFreq * time + phase);
            }
            case SWEEP -> {
                // Экспоненциальный свип
                double sweepFreq = config.getStartFrequency() *
                        Math.pow(config.getEndFrequency() / config.getStartFrequency(), time);
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
                int maxValue = (1 << 23) - 1;
                double scaled = sample * maxValue;
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
}
