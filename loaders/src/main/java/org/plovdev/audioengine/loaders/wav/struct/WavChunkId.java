package org.plovdev.audioengine.loaders.wav.struct;

public enum WavChunkId {
    RIFF("RIFF"), WAVE("WAVE"),
    FMT("fmt "), DATA("data"),
    FACT("fact"), LIST("LIST"),
    CUE("cue "), PLST("plst"),
    LABL("labl"), NOTE("note"),
    LTXT("ltxt"), SMPL("smpl"),
    INST("inst"), BEXT("bext"), // Broadcast Audio Extension
    DISP("DISP"), // Display chunk
    JUNK("JUNK"), // Padding chunk
    INFO("INFO"),
    ICRD("ICRD"),
    INAME("INAM"),
    ISFT("ISFT"),
    UNKNOWN("");


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
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return String.format("[%s]", chunk.toUpperCase().trim());
    }
}