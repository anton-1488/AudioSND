package org.plovdev.audioengine;

import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.getTrackLoaderManager(WavTrackLoaderManager.class).ifPresent(trackLoaderManager -> trackLoaderManager.registerPathLocator(new PathLocator(Path.of("testdata/48000"))));
            Track track = engine.loadTrack("block-story.wav");
            System.out.println(track.getFormat());

            TrackPlayer player = engine.getTrackPlayer(track);
            player.play();

            Thread.sleep(track.getDuration().toMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}