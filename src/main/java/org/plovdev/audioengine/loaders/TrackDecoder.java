package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;

public interface TrackDecoder {
    ByteBuffer decodeToPCM(ByteBuffer input);
    ByteBuffer decodeToFormat(ByteBuffer input, TrackFormat outFormat);
}