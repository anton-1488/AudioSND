package org.plovdev.audioengine.tracks.format;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteOrder;

/**
 * Track format description.
 *
 * @param extension extension of format name.
 * @param channels channels count in audio.
 * @param bitsPerSample auio format width.
 * @param sampleRate auio sample rate.
 * @param signed is signed audio.
 * @param byteOrder audio byte order.
 * @param audioCodec audio data codec.
 *
 * @author Anton
 * @version 1.0
 */

public record TrackFormat(@NotNull @Deprecated String extension, int channels, int bitsPerSample, int sampleRate, boolean signed, @NotNull ByteOrder byteOrder, @NotNull AudioCodec audioCodec) {
    /**
     * Audio codec before converted to pcm
     */
    public enum AudioCodec {
        PCM8,       // 8-bit PCM
        PCM16,      // 16-bit PCM
        PCM24,      // 24-bit PCM
        PCM32,      // 32-bit PCM (integer)
        FLOAT32,    // 32-bit float
        FLOAT64,    // 64-bit float (для Hi-Res)
        ALAW,       // 8-bit A-law
        ULAW,       // 8-bit μ-law
        ADPCM,      // IMA ADPCM
        MP3,        // MPEG Layer III
        AAC,        // Advanced Audio Coding
        OPUS,       // Opus
        VORBIS,     // Ogg Vorbis
        ALAC,       // Apple Lossless
        WAVPACK,     // WavPack lossless
        FLAC,
        OTHER
    }

    public TrackFormat {
        validateField(channels);
        validateField(bitsPerSample);
        validateField(sampleRate);
    }

    private void validateField(int field) {
        if (field <= 0) {
            throw new IllegalArgumentException("Parameter must be greather than 0!");
        }
    }

    /**
     * Calculate bitrate of givven format data.
     * @return audio bitrate.
     */
    public int bitRate() {
        return sampleRate * bitsPerSample * channels;
    }

    @Override
    public String toString() {
        return String.format("%s: %dHz, %dch, %dbit, %s, %s. AudioCodec: %s",
                extension, sampleRate, channels, bitsPerSample,
                signed ? "signed" : "unsigned",
                byteOrder == ByteOrder.BIG_ENDIAN ? "BE" : "LE", audioCodec.name());
    }
}