package org.plovdev.audioengine.generator.config;

import org.plovdev.audioengine.generator.note.Note;
import org.plovdev.audioengine.generator.strategies.envelope.ConstantEnvelope;
import org.plovdev.audioengine.generator.strategies.envelope.EnvelopeStrategy;
import org.plovdev.audioengine.generator.strategies.frequency.ConstantFrequency;
import org.plovdev.audioengine.generator.strategies.frequency.FrequencyStrategy;
import org.plovdev.audioengine.generator.strategies.modulation.ModulationStrategy;
import org.plovdev.audioengine.generator.strategies.noise.NoiseStrategy;
import org.plovdev.audioengine.generator.strategies.wave.SineWave;
import org.plovdev.audioengine.generator.strategies.wave.WaveStrategy;

/**
 * Configuration for audio signal generation using pluggable strategies.
 * <p>
 * This class holds all parameters needed to generate audio signals.
 * Each aspect of generation (frequency, envelope, waveform, etc.) is
 * controlled by a separate strategy interface, allowing flexible
 * combinations of behaviors.
 * </p>
 *
 * <p>
 * The configuration includes:
 * <ul>
 *   <li><b>Frequency strategy</b> - determines how frequency changes over time
 *       (constant, sweep, LFO, etc.)</li>
 *   <li><b>Envelope strategy</b> - controls amplitude changes over time
 *       (ADSR, linear, exponential, etc.)</li>
 *   <li><b>Wave strategy</b> - defines the basic waveform shape
 *       (sine, square, sawtooth, etc.)</li>
 *   <li><b>Modulation strategy</b> - adds LFO effects like vibrato and tremolo</li>
 *   <li><b>Noise strategy</b> - generates noise components
 *       (white, pink, brown, etc.)</li>
 *   <li><b>Stereo strategy</b> - handles channel panning and spatial placement</li>
 * </ul>
 * </p>
 *
 * <p>
 * Basic parameters like phase, duty cycle, and noise level are also
 * configurable and apply across multiple strategy types.
 * </p>
 *
 * @see FrequencyStrategy
 * @see EnvelopeStrategy
 * @see WaveStrategy
 * @see ModulationStrategy
 * @see NoiseStrategy
 *
 * @version 1.0
 * @author Anton
 */
public class GenerationConfig {
    private FrequencyStrategy frequencyStrategy = new ConstantFrequency(2, Note.A4.frequency());
    private EnvelopeStrategy envelopeStrategy = new ConstantEnvelope(Note.A4.amplitude());
    private WaveStrategy waveStrategy = new SineWave();
    private ModulationStrategy modulationStrategy = ((samplePosition, totalSamples, channel, input) -> 0);
    private NoiseStrategy noiseStrategy = (channel -> 0);

    private float phase;
    private float noiseLevel;
    private float pan;

    /**
     * Constructs a new GenerationConfig with all specified strategies and parameters.
     *
     * @param frequencyStrategy   strategy for frequency generation
     * @param envelopeStrategy    strategy for amplitude envelope
     * @param waveStrategy        strategy for waveform shape
     * @param modulationStrategy  strategy for modulation effects
     * @param noiseStrategy       strategy for noise generation
     *
     * @param phase               initial phase in radians
     * @param noiseLevel          mix level for noise (0.0-1.0)
     * @param pan                 generated audio pan
     */
    public GenerationConfig(FrequencyStrategy frequencyStrategy,
                            EnvelopeStrategy envelopeStrategy,
                            WaveStrategy waveStrategy,
                            ModulationStrategy modulationStrategy,
                            NoiseStrategy noiseStrategy,
                            float phase, float noiseLevel, float pan) {
        this.frequencyStrategy = frequencyStrategy;
        this.envelopeStrategy = envelopeStrategy;
        this.waveStrategy = waveStrategy;
        this.modulationStrategy = modulationStrategy;
        this.noiseStrategy = noiseStrategy;

        this.phase = phase;
        this.noiseLevel = noiseLevel;
    }

    /**
     * Default constructor for frameworks that require no-arg construction.
     * Strategies should be set via setters before use.
     */
    public GenerationConfig() {
    }

    /**
     * Creates a new builder instance for constructing GenerationConfig objects.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new ConfigBuilder();
    }

    /**
     * Builder interface for constructing {@link GenerationConfig} instances.
     * <p>
     * Provides fluent API for setting all configuration options.
     * </p>
     */
    public interface Builder {
        /**
         * Sets the frequency strategy.
         *
         * @param strategy the frequency strategy
         * @return this builder
         */
        Builder frequencyStrategy(FrequencyStrategy strategy);

        /**
         * Sets the envelope strategy.
         *
         * @param strategy the envelope strategy
         * @return this builder
         */
        Builder envelopeStrategy(EnvelopeStrategy strategy);

        /**
         * Sets the wave strategy.
         *
         * @param strategy the wave strategy
         * @return this builder
         */
        Builder waveStrategy(WaveStrategy strategy);

        /**
         * Sets the modulation strategy.
         *
         * @param strategy the modulation strategy
         * @return this builder
         */
        Builder modulationStrategy(ModulationStrategy strategy);

        /**
         * Sets the noise strategy.
         *
         * @param strategy the noise strategy
         * @return this builder
         */
        Builder noiseStrategy(NoiseStrategy strategy);

        /**
         * Sets the initial phase.
         *
         * @param phase phase in radians
         * @return this builder
         */
        Builder phase(float phase);

        /**
         * Sets the noise level (mix between pure tone and noise).
         *
         * @param noiseLevel noise level (0.0 = no noise, 1.0 = only noise)
         * @return this builder
         */
        Builder noiseLevel(float noiseLevel);

        Builder pan(float pan);

        /**
         * Builds the GenerationConfig instance with all set parameters.
         *
         * @return the configured GenerationConfig
         */
        GenerationConfig build();
    }

    /**
     * Returns the frequency strategy.
     *
     * @return the frequency strategy
     */
    public FrequencyStrategy getFrequencyStrategy() {
        return frequencyStrategy;
    }

    /**
     * Sets the frequency strategy.
     *
     * @param frequencyStrategy the frequency strategy to set
     */
    public void setFrequencyStrategy(FrequencyStrategy frequencyStrategy) {
        this.frequencyStrategy = frequencyStrategy;
    }

    /**
     * Returns the envelope strategy.
     *
     * @return the envelope strategy
     */
    public EnvelopeStrategy getEnvelopeStrategy() {
        return envelopeStrategy;
    }

    /**
     * Sets the envelope strategy.
     *
     * @param envelopeStrategy the envelope strategy to set
     */
    public void setEnvelopeStrategy(EnvelopeStrategy envelopeStrategy) {
        this.envelopeStrategy = envelopeStrategy;
    }

    /**
     * Returns the wave strategy.
     *
     * @return the wave strategy
     */
    public WaveStrategy getWaveStrategy() {
        return waveStrategy;
    }

    /**
     * Sets the wave strategy.
     *
     * @param waveStrategy the wave strategy to set
     */
    public void setWaveStrategy(WaveStrategy waveStrategy) {
        this.waveStrategy = waveStrategy;
    }

    /**
     * Returns the modulation strategy.
     *
     * @return the modulation strategy
     */
    public ModulationStrategy getModulationStrategy() {
        return modulationStrategy;
    }

    /**
     * Sets the modulation strategy.
     *
     * @param modulationStrategy the modulation strategy to set
     */
    public void setModulationStrategy(ModulationStrategy modulationStrategy) {
        this.modulationStrategy = modulationStrategy;
    }

    /**
     * Returns the noise strategy.
     *
     * @return the noise strategy
     */
    public NoiseStrategy getNoiseStrategy() {
        return noiseStrategy;
    }

    /**
     * Sets the noise strategy.
     *
     * @param noiseStrategy the noise strategy to set
     */
    public void setNoiseStrategy(NoiseStrategy noiseStrategy) {
        this.noiseStrategy = noiseStrategy;
    }

    /**
     * Returns the initial phase.
     *
     * @return phase in radians
     */
    public float getPhase() {
        return phase;
    }

    /**
     * Sets the initial phase.
     *
     * @param phase phase in radians
     */
    public void setPhase(float phase) {
        this.phase = phase;
    }

    /**
     * Returns the noise level.
     *
     * @return noise level (0.0-1.0)
     */
    public float getNoiseLevel() {
        return noiseLevel;
    }

    /**
     * Sets the noise level.
     *
     * @param noiseLevel noise level (0.0-1.0)
     */
    public void setNoiseLevel(float noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public float getPan() {
        return pan;
    }

    public void setPan(float pan) {
        this.pan = pan;
    }
}