package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            Track track = engine.loadTrack("testdata/48000/block-story.wav");
            engine.getTrackPlayer(track).play();
            Thread.sleep(track.getDuration().toMillis());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}