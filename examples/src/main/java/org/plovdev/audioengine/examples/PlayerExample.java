package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;


public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    void main() {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.getTrackLoaderManager(WavTrackLoaderManager.class).ifPresent(m -> m.registerPathLocator(new PathLocator(Path.of("testdata/wav/48000/24"))));

            Track track = engine.loadTrack(new File("Cotton Eye Joy.wav"));
            System.out.println(track.getMetaData());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}