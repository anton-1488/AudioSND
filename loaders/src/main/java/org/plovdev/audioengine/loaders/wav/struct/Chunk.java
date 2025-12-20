package org.plovdev.audioengine.loaders.wav.struct;

import java.util.Objects;

public class Chunk {
    protected WavChunkId chunk;
    protected int chunkSize;
    protected byte[] body;

    public Chunk() {
    }

    public Chunk(WavChunkId chunk, int size, byte[] bytes) {
        this.chunk = chunk;
        this.chunkSize = size;
        body = bytes;
    }

    public WavChunkId getChunk() {
        return chunk;
    }

    public void setChunk(WavChunkId chunk) {
        this.chunk = chunk;
    }

    public int getSize() {
        return chunkSize;
    }

    public void setSize(int size) {
        this.chunkSize = size;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return chunkSize == chunk.chunkSize && this.chunk == chunk.chunk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkSize);
    }

    @Override
    public String toString() {
        return String.format("%s chunk, size: %d", chunk.toString(), chunkSize);
    }
}