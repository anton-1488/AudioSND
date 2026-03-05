package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.effects.EffectsChain;
import org.plovdev.audioengine.effects.GainEffect;
import org.plovdev.audioengine.generator.TrackGenerationFactory;
import org.plovdev.audioengine.generator.note.Note;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class GenerationExample {
    private static final Logger log = LoggerFactory.getLogger(GenerationExample.class);
    private static final AudioEngine engine = new NativeAudioEngine();

    void main() {
        Track track = TrackGenerationFactory.generateSineWave(Duration.ofSeconds(5), WavTrackFormatFactory.wav16bitStereo48kHz(), Note.E2);
        track = new EffectsChain().addEffect(new GainEffect(5)).apply(track);
        play(track);
    }
    private static void play(Track track) {
        try {
            engine.createTrackPlayer(track).play();
            Thread.sleep(track.getDuration());
        } catch (Exception e) {
            log.error("Playing error: ", e);
        }
    }
}