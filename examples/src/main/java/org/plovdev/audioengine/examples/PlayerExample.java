package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.getTrackLoaderManager(WavTrackLoaderManager.class).ifPresent(m -> m.registerPathLocator(new PathLocator(Path.of("testdata/wav/48000/24"))));

            Track track1 = engine.loadTrack("cotton-doe.wav");
            System.out.println(track1.getMetaData());
            System.out.println(track1.getFormat());

<<<<<<< HEAD
            System.out.println(track1.getDuration());

            engine.getTrackPlayer(track1).play();
=======
>>>>>>> fix/niad
            Thread.sleep(track1.getDuration());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}