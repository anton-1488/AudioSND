package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

public class Mp3TrackFormatFactory {
    private Mp3TrackFormatFactory() {}

    public static TrackFormat mp3Stereo64kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,     // compressed
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat mp3Stereo128kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat mp3Stereo192kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat mp3Stereo320kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }
}