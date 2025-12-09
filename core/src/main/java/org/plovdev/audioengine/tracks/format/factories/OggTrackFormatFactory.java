package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

public class OggTrackFormatFactory {
    private OggTrackFormatFactory() {}

    public static TrackFormat oggVorbisStereo44kHz() {
        return new TrackFormat(
                "ogg",
                2,
                0,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat oggOpusStereo48kHz() {
        return new TrackFormat(
                "opus",
                2,
                0,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }
}