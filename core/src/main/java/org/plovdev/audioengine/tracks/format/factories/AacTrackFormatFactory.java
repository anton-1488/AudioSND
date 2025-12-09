package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

public class AacTrackFormatFactory {
    private AacTrackFormatFactory() {}

    public static TrackFormat aacStereo128kbps() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat aacStereo256kbps() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }
}