package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

public class AiffTrackFormatFactory {
    public static TrackFormat aiff16bitStereo44kHz() {
        return new TrackFormat(
                "aiff",
                2,
                16,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat aiff24bitStereo48kHz() {
        return new TrackFormat(
                "aiff",
                2,
                24,
                48000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }
}