package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.mixer.NativeTrackMixer;
import org.plovdev.audioengine.mixer.TrackMixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixerExample {
    private static final Logger log = LoggerFactory.getLogger(MixerExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            TrackMixer mixer = new NativeTrackMixer();

        } catch (Exception e) {
            log.error("AudioEngine error: ", e);
        }
    }
}