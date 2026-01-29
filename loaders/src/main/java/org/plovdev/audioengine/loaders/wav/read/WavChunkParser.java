package org.plovdev.audioengine.loaders.wav.read;

import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

public interface WavChunkParser {
    boolean canParse(WavChunkId id);
    Chunk parse(byte[] body);
}