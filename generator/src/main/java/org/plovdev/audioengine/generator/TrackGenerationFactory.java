package org.plovdev.audioengine.generator;

import org.plovdev.audioengine.generator.config.GenerationConfig;
import org.plovdev.audioengine.generator.note.Note;
import org.plovdev.audioengine.generator.strategies.envelope.*;
import org.plovdev.audioengine.generator.strategies.frequency.*;
import org.plovdev.audioengine.generator.strategies.modulation.LFOModulation;
import org.plovdev.audioengine.generator.strategies.noise.*;
import org.plovdev.audioengine.generator.strategies.wave.*;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.time.Duration;

/**
 * Factory for creating common audio generator configurations.
 *
 * @version 1.0
 * @author Anton
 */
public class TrackGenerationFactory {

    // ============ BASIC WAVES ============

    /**
     * Creates a sine wave tone from a note.
     */
    public static Track generateSineWave(Duration duration, TrackFormat format, Note note) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a square wave tone from a note.
     */
    public static Track generateSquareWave(Duration duration, TrackFormat format, Note note, float dutyCycle) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SquareWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .dutyCycle(dutyCycle)
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a sawtooth wave tone from a note.
     */
    public static Track generateSawtoothWave(Duration duration, TrackFormat format, Note note) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SawtoothWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a triangle wave tone from a note.
     */
    public static Track generateTriangleWave(Duration duration, TrackFormat format, Note note) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new TriangleWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ NOISE ============

    /**
     * Creates white noise.
     */
    public static Track generateWhiteNoise(Duration duration, TrackFormat format, float amplitude) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), 0))
                .waveStrategy(new SineWave()) // not used when noiseLevel=1
                .envelopeStrategy(new ConstantEnvelope(amplitude))
                .noiseStrategy(new WhiteNoise())
                .noiseLevel(1.0f)
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates pink noise.
     */
    public static Track generatePinkNoise(Duration duration, TrackFormat format, float amplitude) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), 0))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(amplitude))
                .noiseStrategy(new PinkNoise())
                .noiseLevel(1.0f)
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates brown noise.
     */
    public static Track generateBrownNoise(Duration duration, TrackFormat format, float amplitude) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), 0))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(amplitude))
                .noiseStrategy(new BrownNoise())
                .noiseLevel(1.0f)
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ FREQUENCY SWEEPS ============

    /**
     * Creates a linear frequency sweep.
     */
    public static Track generateLinearSweep(Duration duration, TrackFormat format,
                                            float startFreq, float endFreq, float amplitude) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new LinearFrequency(format.channels(), startFreq, endFreq))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(amplitude))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a linear frequency sweep with square wave.
     */
    public static Track generateSquareSweep(Duration duration, TrackFormat format,
                                            float startFreq, float endFreq, float amplitude) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new LinearFrequency(format.channels(), startFreq, endFreq))
                .waveStrategy(new SquareWave())
                .envelopeStrategy(new ConstantEnvelope(amplitude))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ WITH ENVELOPES ============

    /**
     * Creates a tone with ADSR envelope.
     */
    public static Track generateWithADSR(Duration duration, TrackFormat format, Note note,
                                         float attack, float decay, float sustain, float release) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ADSRStrategy(attack, decay, sustain, release))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a tone with linear fade in/out.
     */
    public static Track generateWithFade(Duration duration, TrackFormat format, Note note,
                                         float fadeIn, float fadeOut) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new LinearEnvelope(fadeIn, fadeOut))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates an exponentially decaying tone.
     */
    public static Track generateExponentialDecay(Duration duration, TrackFormat format, Note note, float decayFactor) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ExponentialEnvelope(decayFactor))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a tone with Hann window envelope.
     */
    public static Track generateHannWindow(Duration duration, TrackFormat format, Note note) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new HannEnvelope())
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ WITH MODULATION ============

    /**
     * Creates a tone with vibrato (frequency modulation).
     */
    public static Track generateWithVibrato(Duration duration, TrackFormat format, Note note,
                                            float rate, float depth) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .modulationStrategy(new LFOModulation(rate, depth, format.sampleRate()))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ WITH PAN ============

    /**
     * Creates a tone with stereo panning.
     */
    public static Track generateWithPan(Duration duration, TrackFormat format, Note note, float pan) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .pan(pan)
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ COMBINED ============

    /**
     * Creates a complex sound with sawtooth wave, ADSR envelope and vibrato.
     */
    public static Track generateComplexTone(Duration duration, TrackFormat format, Note note,
                                            float attack, float decay, float sustain, float release,
                                            float vibratoRate, float vibratoDepth) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SawtoothWave())
                .envelopeStrategy(new ADSRStrategy(attack, decay, sustain, release))
                .modulationStrategy(new LFOModulation(vibratoRate, vibratoDepth, format.sampleRate()))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a tone with noise mixed in.
     */
    public static Track generateWithNoise(Duration duration, TrackFormat format, Note note,
                                          float noiseLevel, NoiseStrategy noiseStrategy) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .noiseStrategy(noiseStrategy)
                .noiseLevel(noiseLevel)
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Creates a silent track.
     */
    public static Track generateSilence(Duration duration, TrackFormat format) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), 0))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ConstantEnvelope(0))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    // ============ INSTRUMENT PRESETS ============

    /**
     * Simple piano-like sound.
     */
    public static Track generatePianoLike(Duration duration, TrackFormat format, Note note) {
        return generateWithADSR(duration, format, note, 0.01f, 0.1f, 0.0f, 0.2f);
    }

    /**
     * Simple guitar-like sound.
     */
    public static Track generateGuitarLike(Duration duration, TrackFormat format, Note note) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SquareWave())
                .envelopeStrategy(new ADSRStrategy(0.005f, 0.3f, 0.0f, 0.1f))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }

    /**
     * Simple flute-like sound.
     */
    public static Track generateFluteLike(Duration duration, TrackFormat format, Note note) {
        GenerationConfig config = GenerationConfig.builder()
                .frequencyStrategy(new ConstantFrequency(format.channels(), note.frequency()))
                .waveStrategy(new SineWave())
                .envelopeStrategy(new ADSRStrategy(0.1f, 0.1f, 0.8f, 0.2f))
                .build();

        return new TrackGenerator(config, format).generate(duration);
    }
}