package org.plovdev.audioengine.utils.format;

import java.nio.ByteOrder;
import java.util.Objects;

public record TrackFormat(String extension, int channels, int bitsPerSample, int sampleRate, boolean signed, ByteOrder byteOrder) {
    public int bitRate() {
        return sampleRate * bitsPerSample * channels;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrackFormat that = (TrackFormat) object;
        return channels == that.channels &&
                bitsPerSample == that.bitsPerSample &&
                sampleRate == that.sampleRate &&
                signed == that.signed &&
                Objects.equals(extension, that.extension);
    }

    @Override
    public String toString() {
        return String.format("%s: %dHz, %dch, %dbit, %s, %s",
                extension, sampleRate, channels, bitsPerSample,
                signed ? "signed" : "unsigned",
                byteOrder == ByteOrder.BIG_ENDIAN ? "BE" : "LE");
    }
}