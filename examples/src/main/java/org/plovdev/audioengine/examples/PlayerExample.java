package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.raw.RawTrackLoader;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;


public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    void main() {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.getTrackLoaderManager(WavTrackLoaderManager.class).ifPresent(m -> m.registerPathLocator(new PathLocator(Path.of("testdata/wav/test"))));

            RawTrackLoader loader = new RawTrackLoader(WavTrackFormatFactory.raw16bitStereo44kHz());
            Track track = loader.loadTrack(new File("ph.raw"));
            engine.getTrackPlayer(track).play();
            Thread.sleep(track.getDuration());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}