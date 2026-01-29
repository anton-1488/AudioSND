package org.plovdev.audioengine.loaders.wav.chunks;

import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

public class TagEntry extends Chunk {
    private String content;

    public TagEntry(String content) {
        this.content = content;
    }

    public TagEntry(WavChunkId chunk, int size, byte[] bytes, String content) {
        super(chunk, size, bytes);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}