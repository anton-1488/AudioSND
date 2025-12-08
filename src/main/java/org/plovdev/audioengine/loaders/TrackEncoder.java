package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;

public interface TrackEncoder {
    ByteBuffer encodeFromPCM(ByteBuffer input);
    ByteBuffer encodeFromFormat(ByteBuffer input, TrackFormat outFormat);
}