package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

/**
 * Factory of commons track formats.
 * Contains different methods to create different audio.
 *
 * @author Anton
 * @version 1.0
 */
public class TrackFormatFactory {
    public static TrackFormat dsd64Stereo() {
        return new TrackFormat(
                "dsf",
                2,
                1,     // DSD 1-bit
                2822400, // 2.8224 MHz
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat gameAudio22050Hz() {
        return new TrackFormat(
                "wav",
                1,
                16,
                22050,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat gameAudioStereo32000Hz() {
        return new TrackFormat(
                "wav",
                2,
                16,
                32000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== Телефонные/коммуникационные форматы ====

    public static TrackFormat telephoneMono8kHz() {
        return new TrackFormat(
                "wav",
                1,
                16,
                8000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat gsm6100() {
        return new TrackFormat(
                "gsm",
                1,
                0,
                8000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== Мультиканальные форматы (surround) ====

    public static TrackFormat surround51_24bit48kHz() {
        return new TrackFormat(
                "wav",
                6,     // 5.1 surround
                24,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat surround71_24bit48kHz() {
        return new TrackFormat(
                "wav",
                8,     // 7.1 surround
                24,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat dolbyAtmos_32bitFloat48kHz() {
        return new TrackFormat(
                "wav",
                12,    // Dolby Atmos bed + objects
                32,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat webmOpusStereo48kHz() {
        return new TrackFormat(
                "webm",
                2,
                0,     // compressed
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat m4aAacStereo256kbps() {
        return new TrackFormat(
                "m4a",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }
}