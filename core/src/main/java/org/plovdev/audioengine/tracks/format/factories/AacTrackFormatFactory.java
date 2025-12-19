package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

/**
 * Factory of .aac(and simily) formats.
 * Contains different methods to create aac audio.
 *
 * @author Anton
 * @version 1.0
 */
public class AacTrackFormatFactory {
    private AacTrackFormatFactory() {}

    public static TrackFormat aacStereo128kbps() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN,
                TrackFormat.AudioCodec.AAC
        );
    }

    public static TrackFormat aacStereo256kbps() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN,
                TrackFormat.AudioCodec.AAC
        );
    }
}