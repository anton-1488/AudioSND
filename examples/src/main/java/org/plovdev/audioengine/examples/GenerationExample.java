package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class GenerationExample {
    private static final Logger log = LoggerFactory.getLogger(GenerationExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            Track track = engine.loadTrack(new URI("https://opengameart.org/sites/default/files/Menu%20Selection%20Click.wav"));
            System.out.println(track.getMetaData());
            engine.getTrackPlayer(track).play();
            Thread.sleep(track.getDuration());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}