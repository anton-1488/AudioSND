package org.plovdev.audioengine.tracks.format;

import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Track format description.
 *
 * @param extension extension of format name.
 * @param channels channels count in audio.
 * @param bitsPerSample auio format width.
 * @param sampleRate auio sample rate.
 * @param signed is signed audio.
 * @param byteOrder audio byte order.
 *
 * @author Anton
 * @version 1.0
 */
public record TrackFormat(String extension, int channels, int bitsPerSample, int sampleRate, boolean signed, ByteOrder byteOrder, AudioCodec audioCodec) {
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

    /**
     * Calculate bitrate of givven format data.
     * @return audio bitrate.
     */
    public int bitRate() {
        return sampleRate * bitsPerSample * channels;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackFormat that = (TrackFormat) o;
        return channels == that.channels && sampleRate == that.sampleRate && signed == that.signed && bitsPerSample == that.bitsPerSample && Objects.equals(extension, that.extension) && Objects.equals(byteOrder, that.byteOrder) && Objects.equals(audioCodec, that.audioCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension, channels, bitsPerSample, sampleRate, signed, byteOrder);
    }

    @Override
    public String toString() {
        return String.format("%s: %dHz, %dch, %dbit, %s, %s. AudioCodec: %s",
                extension, sampleRate, channels, bitsPerSample,
                signed ? "signed" : "unsigned",
                byteOrder == ByteOrder.BIG_ENDIAN ? "BE" : "LE", audioCodec.name());
    }
}