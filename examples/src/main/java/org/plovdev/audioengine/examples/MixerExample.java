package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.mixer.NativeTrackMixer;
import org.plovdev.audioengine.mixer.TrackMixer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MixerExample {
    private static final Logger log = LoggerFactory.getLogger(MixerExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            TrackMixer mixer = new NativeTrackMixer();
            mixer.setOutputFormat(WavTrackFormatFactory.wav32bitFloatStereo96kHz());

            mixer.addTrack(engine.loadTrack(new File("testdata/wav/48000/24/melody1-stereo.wav")));
            mixer.addTrack(engine.loadTrack(new File("testdata/wav/48000/24/pornhub-stereo.wav")));

            Track mixed = mixer.doMixing();

            engine.getTrackPlayer(mixed).play();
            Thread.sleep(mixed.getDuration());
        } catch (Exception e) {
            log.error("AudioEngine error: ", e);
        }
    }
}