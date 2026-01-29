package org.plovdev.audioengine.loaders.wav.chunks;

import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

import java.util.List;

public class ListChunk extends Chunk {
    private WavChunkId chunkType;
    private List<Chunk> entries;

    public ListChunk() {
    }

    public ListChunk(WavChunkId chunkType, List<Chunk> entries) {
        this.chunkType = chunkType;
        this.entries = entries;
    }

    public ListChunk(WavChunkId chunk, int size, byte[] bytes, WavChunkId chunkType, List<Chunk> entries) {
        super(chunk, size, bytes);
        this.chunkType = chunkType;
        this.entries = entries;
    }

    public WavChunkId getChunkType() {
        return chunkType;
    }

    public void setChunkType(WavChunkId chunkType) {
        this.chunkType = chunkType;
    }

    public List<Chunk> getEntries() {
        return entries;
    }

    public void setEntries(List<Chunk> entries) {
        this.entries = entries;
    }
}