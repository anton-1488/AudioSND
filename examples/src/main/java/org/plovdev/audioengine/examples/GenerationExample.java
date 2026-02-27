package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.generator.Note;
import org.plovdev.audioengine.generator.TrackGenerator;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class GenerationExample {
    private static final Logger log = LoggerFactory.getLogger(GenerationExample.class);
    private static final AudioEngine engine = new NativeAudioEngine();

    void main() {
        Track track1 = TrackGenerator.generateSine(WavTrackFormatFactory.wav16bitStereo44kHz(), Duration.ofSeconds(10), Note.A2); // generate A2
        Track track2 = TrackGenerator.generateSine(WavTrackFormatFactory.wav24bitStereo48kHz(), Duration.ofSeconds(5), Note.C2, Note.E2, Note.G2); // generate chord
        Track track3 = TrackGenerator.generateNoise(WavTrackFormatFactory.wav16bitMono44kHz(), Duration.ofSeconds(10));
        Track track4 = TrackGenerator.generateChirp(WavTrackFormatFactory.wav16bitStereo48kHz(), Duration.ofSeconds(7), Note.C2, Note.C2.upOctave());
        Track track5 = TrackGenerator.generateImpulse(WavTrackFormatFactory.wav16bitStereo44kHz(), Duration.ofSeconds(10));
        Track track6 = TrackGenerator.generateSawtooth(WavTrackFormatFactory.wav24bitStereo44kHz(), Duration.ofSeconds(10), Note.C2.transpose(1)); // +1 semitone
        Track track7 = TrackGenerator.generateSilence(WavTrackFormatFactory.wav16bitStereo44kHz(), Duration.ofSeconds(20));
        Track track8 = TrackGenerator.generateSquare(WavTrackFormatFactory.wav24bitStereo96kHz(), Duration.ofSeconds(10), Note.E2, 0.5);
        Track track9 = TrackGenerator.generateSweep(WavTrackFormatFactory.wav32bitFloatStereo48kHz(), Duration.ofSeconds(10), Note.G2, Note.C2);
        Track track10 = TrackGenerator.generateTriangle(WavTrackFormatFactory.wav32bitFloatStereo44kHz(), Duration.ofSeconds(5), Note.A2);
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