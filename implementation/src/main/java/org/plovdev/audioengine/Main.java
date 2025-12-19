package org.plovdev.audioengine;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            Track track = engine.loadTrack("testdata/48000/block-story.wav");

            TrackPlayer player = engine.getTrackPlayer(track);
            player.play();

            Thread.sleep(track.getDuration().toMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}