package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

public class FlacTrackFormatFactory {
    private FlacTrackFormatFactory() {}

    public static TrackFormat flac16bitStereo44kHz() {
        return new TrackFormat(
                "flac",
                2,
                16,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat flac24bitStereo96kHz() {
        return new TrackFormat(
                "flac",
                2,
                24,
                96000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }
}