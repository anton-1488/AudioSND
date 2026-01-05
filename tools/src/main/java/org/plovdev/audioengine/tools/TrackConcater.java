package org.plovdev.audioengine.tools;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TrackConcater {
    private final List<Track> tracks = new ArrayList<>();

    public TrackConcater() {}


    public void addTrack(Track track) {
        tracks.add(track);
    }
    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    public Track concate() {
        long totalMillis = 0;
        int totalSize = 0;

        TrackFormat format = WavTrackFormatFactory.wav16bitStereo44kHz();

        if (tracks.isEmpty()) return null;

        for (Track track : tracks) {
            totalMillis += track.getDuration().toMillis();
            totalSize += track.getTrackData().limit();
            format = track.getFormat();
        }

        ByteBuffer total = ByteBuffer.allocateDirect(totalSize);

        for (Track track : tracks) {
            total.put(track.getTrackData());
        }

        return new Track(total, Duration.ofMillis(totalMillis), format, new TrackMetadata());
    }
}