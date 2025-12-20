package org.plovdev.audioengine.loaders.wav.struct;

public enum WavChunkId {
    RIFF("RIFF"), WAVE("WAVE"), NULL("NULL"),
    DATA("data"), FORMAT("fmt "), INFO("info");


    private final String chunk;
    WavChunkId(String ch) {
        chunk = ch;
    }

    public String getChunk() {
        return chunk;
    }

    public static WavChunkId fromString(String name) {
        for (WavChunkId chunkId : values()) {
            if (chunkId.getChunk().trim().equalsIgnoreCase(name.trim())) {
                return chunkId;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("[%s]", chunk.toUpperCase().trim());
    }
}