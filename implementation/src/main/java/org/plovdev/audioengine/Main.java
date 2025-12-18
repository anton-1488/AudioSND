package org.plovdev.audioengine;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.plovdev.audioengine.wav.WavTrackLoaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.addLoaderManager(new WavTrackLoaderManager());
            Track track = engine.loadTrack("testdata/block-story.wav");
            System.out.println(track.getFormat());
            TrackPlayer player = engine.getTrackPlayer(track);
            player.play();
            Thread.sleep(track.getDuration().toMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}