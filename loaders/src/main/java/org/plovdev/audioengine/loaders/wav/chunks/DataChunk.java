package org.plovdev.audioengine.loaders.wav.chunks;

import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.utils.AudioEngineUtils;

import java.nio.ByteBuffer;

public class DataChunk extends Chunk {
    private ByteBuffer data;

    public DataChunk(ByteBuffer buffer) {
        super(WavChunkId.DATA, buffer.remaining(), AudioEngineUtils.directBufferToBytes(buffer));
        data = buffer;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}