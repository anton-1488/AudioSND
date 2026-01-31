package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.profiler.ExecutionBenchmarker;
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

            long delay = ExecutionBenchmarker.testExecutionDelay(() -> engine.loadTrack(new File("block-story-stereo.wav"))) / 1000000;
            log.info("Loading delay: {}ms", delay);

            Track track = engine.loadTrack(new File("cotton-doe.wav"));
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}