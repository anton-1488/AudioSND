package org.plovdev.audioengine.generator.config;

import org.plovdev.audioengine.generator.strategies.envelope.EnvelopeStrategy;
import org.plovdev.audioengine.generator.strategies.frequency.FrequencyStrategy;
import org.plovdev.audioengine.generator.strategies.modulation.ModulationStrategy;
import org.plovdev.audioengine.generator.strategies.noise.NoiseStrategy;
import org.plovdev.audioengine.generator.strategies.stereo.StereoStrategy;
import org.plovdev.audioengine.generator.strategies.wave.WaveStrategy;

/**
 * Builder implementation for {@link GeneratorConfig}.
 * <p>
 * Provides a fluent API for constructing GeneratorConfig instances with
 * all required strategies and parameters. This builder follows the
 * builder pattern and is typically obtained via
 * {@link GeneratorConfig#builder()}.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * GeneratorConfig config = new ConfigBuilder()
 *     .frequencyStrategy(new ConstantFrequency(440.0f))
 *     .waveStrategy(new SineWaveStrategy())
 *     .envelopeStrategy(new ConstantEnvelope(0.5f))
 *     .phase(0.0f)
 *     .build();
 * </pre>
 * </p>
 *
 * @see GeneratorConfig
 * @see GeneratorConfig.Builder
 */
public class ConfigBuilder implements GeneratorConfig.Builder {
    private FrequencyStrategy frequencyStrategy;
    private EnvelopeStrategy envelopeStrategy;
    private WaveStrategy waveStrategy;
    private ModulationStrategy modulationStrategy;
    private NoiseStrategy noiseStrategy;
    private StereoStrategy stereoStrategy;

    private float phase;
    private float dutyCycle;
    private float noiseLevel;

    /**
     * Sets the frequency strategy.
     *
     * @param strategy the frequency generation strategy
     * @return this builder
     */
    @Override
    public GeneratorConfig.Builder frequencyStrategy(FrequencyStrategy strategy) {
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
    public GeneratorConfig.Builder envelopeStrategy(EnvelopeStrategy strategy) {
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
    public GeneratorConfig.Builder waveStrategy(WaveStrategy strategy) {
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
    public GeneratorConfig.Builder modulationStrategy(ModulationStrategy strategy) {
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
    public GeneratorConfig.Builder noiseStrategy(NoiseStrategy strategy) {
        noiseStrategy = strategy;
        return this;
    }

    /**
     * Sets the stereo strategy.
     *
     * @param strategy the stereo panning strategy
     * @return this builder
     */
    @Override
    public GeneratorConfig.Builder stereoStereategy(StereoStrategy strategy) {
        stereoStrategy = strategy;
        return this;
    }

    /**
     * Sets the initial phase.
     *
     * @param phase phase in radians
     * @return this builder
     */
    @Override
    public GeneratorConfig.Builder phase(float phase) {
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
    public GeneratorConfig.Builder dutyCycle(float dc) {
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
    public GeneratorConfig.Builder noiseLevel(float noiseLevel) {
        this.noiseLevel = noiseLevel;
        return this;
    }

    /**
     * Builds the GeneratorConfig instance with all configured strategies and parameters.
     * <p>
     * Note: Some strategies may be required depending on the use case.
     * The caller is responsible for ensuring all necessary strategies are set.
     * </p>
     *
     * @return a new GeneratorConfig instance
     */
    @Override
    public GeneratorConfig build() {
        return new GeneratorConfig(frequencyStrategy, envelopeStrategy, waveStrategy, modulationStrategy, noiseStrategy, stereoStrategy, phase, dutyCycle, noiseLevel);
    }
}