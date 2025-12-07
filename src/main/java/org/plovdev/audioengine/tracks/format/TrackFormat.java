package main.java.org.plovdev.audioengine.tracks.format;

import java.nio.ByteOrder;
import java.util.Objects;

public record TrackFormat(String extension, int channels, int bitsPerSample, int sampleRate, boolean signed, ByteOrder byteOrder) {
    public int bitRate() {
        return sampleRate * bitsPerSample * channels;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackFormat that = (TrackFormat) o;
        return channels == that.channels && sampleRate == that.sampleRate && signed == that.signed && bitsPerSample == that.bitsPerSample && Objects.equals(extension, that.extension) && Objects.equals(byteOrder, that.byteOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension, channels, bitsPerSample, sampleRate, signed, byteOrder);
    }

    @Override
    public String toString() {
        return String.format("%s: %dHz, %dch, %dbit, %s, %s",
                extension, sampleRate, channels, bitsPerSample,
                signed ? "signed" : "unsigned",
                byteOrder == ByteOrder.BIG_ENDIAN ? "BE" : "LE");
    }
}