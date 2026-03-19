package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.api.TrackPlayer;
import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.effects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    void main() {
        try (AudioEngine engine = new NativeAudioEngine()) {
            Track track = engine.loadTrack(new File("testdata/last-hero.wav"));
            track = new EffectsChain().addEffect(new OverdriveEffect()).apply(track);
            engine.exportTrack(track, new FileOutputStream("last-hero-ov.wav"));

            TrackPlayer player = engine.createTrackPlayer(track);
            player.play();
            Thread.sleep(track.getDuration());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}