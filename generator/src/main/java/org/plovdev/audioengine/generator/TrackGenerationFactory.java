package org.plovdev.audioengine.generator;

import org.plovdev.audioengine.generator.config.GeneratorConfig;
import org.plovdev.audioengine.generator.note.Note;
import org.plovdev.audioengine.generator.strategies.envelope.ConstantEnvelope;
import org.plovdev.audioengine.generator.strategies.frequency.LinearFrequncy;
import org.plovdev.audioengine.generator.strategies.wave.SineWave;
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
    /**
     * Creates a sine wave tone from a note.
     *
     * @param duration duration of generated track.
     * @param format track format.
     * @param note musical note.
     *
     * @return configured generator.
     */
    public static Track generateSineWave(Duration duration, TrackFormat format, Note note) {
        GeneratorConfig config = GeneratorConfig.builder()
                .frequencyStrategy(new LinearFrequncy(format.channels(), note.frequency()))
                .waveStrategy(new SineWave(format.sampleRate()))
                .envelopeStrategy(new ConstantEnvelope(note.amplitude()))
                .build();

        TrackGenerator generator = new TrackGenerator(config, format);
        return generator.generate(duration);
    }
}