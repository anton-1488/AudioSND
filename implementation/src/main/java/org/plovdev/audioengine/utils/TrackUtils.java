package org.plovdev.audioengine.utils;

import org.plovdev.audioengine.tracks.Track;

import java.nio.ByteBuffer;

public class TrackUtils {
    private TrackUtils() {}

    public static byte[] getTrackBytes(Track track) {
        ByteBuffer directData = track.getTrackData();
        byte[] bytes = new byte[directData.remaining()];
        directData.get(bytes);

        return bytes;
    }
}