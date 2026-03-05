package org.plovdev.audioengine.generator.config;

import org.plovdev.audioengine.generator.strategies.envelope.EnvelopeStrategy;
import org.plovdev.audioengine.generator.strategies.frequency.FrequencyStrategy;
import org.plovdev.audioengine.generator.strategies.modulation.ModulationStrategy;
import org.plovdev.audioengine.generator.strategies.noise.NoiseStrategy;
import org.plovdev.audioengine.generator.strategies.wave.WaveStrategy;

/**
 * Builder implementation for {@link GenerationConfig}.
 * <p>
 * Provides a fluent API for constructing GenerationConfig instances with
 * all required strategies and parameters. This builder follows the
 * builder pattern and is typically obtained via
 * {@link GenerationConfig#builder()}.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * GenerationConfig config = new ConfigBuilder()
 *     .frequencyStrategy(new ConstantFrequency(440.0f))
 *     .waveStrategy(new SineWaveStrategy())
 *     .envelopeStrategy(new ConstantEnvelope(0.5f))
 *     .phase(0.0f)
 *     .build();
 * </pre>
 * </p>
 *
 * @see GenerationConfig
 * @see GenerationConfig.Builder
 */
public class ConfigBuilder implements GenerationConfig.Builder {
    private FrequencyStrategy frequencyStrategy;
    private EnvelopeStrategy envelopeStrategy;
    private WaveStrategy waveStrategy;
    private ModulationStrategy modulationStrategy;
    private NoiseStrategy noiseStrategy;

    private float phase;
    private float dutyCycle;
    private float noiseLevel;
    private float pan;

    /**
     * Sets the frequency strategy.
     *
     * @param strategy the frequency generation strategy
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder frequencyStrategy(FrequencyStrategy strategy) {
        frequencyStrategy = strategy;
        return this;
    }

    /**
     * Sets the envelope strategy.
     *
     * @param strategy the amplitude envelope strategy
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder envelopeStrategy(EnvelopeStrategy strategy) {
        envelopeStrategy = strategy;
        return this;
    }

    /**
     * Sets the wave strategy.
     *
     * @param strategy the waveform shape strategy
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder waveStrategy(WaveStrategy strategy) {
        waveStrategy = strategy;
        return this;
    }

    /**
     * Sets the modulation strategy.
     *
     * @param strategy the modulation (LFO) strategy
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder modulationStrategy(ModulationStrategy strategy) {
        modulationStrategy = strategy;
        return this;
    }

    /**
     * Sets the noise strategy.
     *
     * @param strategy the noise generation strategy
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder noiseStrategy(NoiseStrategy strategy) {
        noiseStrategy = strategy;
        return this;
    }

    /**
     * Sets the initial phase.
     *
     * @param phase phase in radians
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder phase(float phase) {
        this.phase = phase;
        return this;
    }

    /**
     * Sets the duty cycle for square wave generation.
     *
     * @param dc duty cycle (0.0-1.0, 0.5 = square wave)
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder dutyCycle(float dc) {
        this.dutyCycle = dc;
        return this;
    }

    /**
     * Sets the noise level.
     *
     * @param noiseLevel noise level (0.0 = no noise, 1.0 = only noise)
     * @return this builder
     */
    @Override
    public GenerationConfig.Builder noiseLevel(float noiseLevel) {
        this.noiseLevel = noiseLevel;
        return this;
    }

    @Override
    public GenerationConfig.Builder pan(float pan) {
        this.pan = pan;
        return this;
    }

    /**
     * Builds the GenerationConfig instance with all configured strategies and parameters.
     * <p>
     * Note: Some strategies may be required depending on the use case.
     * The caller is responsible for ensuring all necessary strategies are set.
     * </p>
     *
     * @return a new GenerationConfig instance
     */
    @Override
    public GenerationConfig build() {
        return new GenerationConfig(frequencyStrategy, envelopeStrategy, waveStrategy, modulationStrategy, noiseStrategy, phase, dutyCycle, noiseLevel, pan);
    }
}