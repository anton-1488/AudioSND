package org.plovdev.audioengine.generator;

import java.util.Arrays;

public class GeneratorConfig {
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

    public double[] getFrequencies() {
        return frequencies.clone();
    }

    public double[] getAmplitudes() {
        return amplitudes.clone();
    }

    public WaveType getWaveType() {
        return waveType;
    }

    public double getPhase() {
        return phase;
    }

    public double getDutyCycle() {
        return dutyCycle;
    }

    public double getNoiseLevel() {
        return noiseLevel;
    }

    public boolean isFrequencySweep() {
        return frequencySweep;
    }

    public double getStartFrequency() {
        return startFrequency;
    }

    public double getEndFrequency() {
        return endFrequency;
    }

    public boolean isAmplitudeEnvelope() {
        return amplitudeEnvelope;
    }

    public EnvelopeType getEnvelopeType() {
        return envelopeType;
    }
}