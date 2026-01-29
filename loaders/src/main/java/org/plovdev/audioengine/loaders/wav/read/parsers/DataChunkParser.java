package org.plovdev.audioengine.loaders.wav.read.parsers;

import org.plovdev.audioengine.loaders.wav.chunks.DataChunk;
import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

import java.nio.ByteBuffer;

public class DataChunkParser implements WavChunkParser {
    @Override
    public boolean canParse(WavChunkId id) {
        return id == WavChunkId.DATA;
    }

    @Override
    public Chunk parse(byte[] body) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(body.length);
        buffer.put(body);
        buffer.flip();
        return new DataChunk(buffer);
    }
}