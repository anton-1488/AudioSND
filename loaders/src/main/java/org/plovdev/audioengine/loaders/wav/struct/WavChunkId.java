package org.plovdev.audioengine.loaders.wav.struct;

import java.util.List;

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
    TBPM("TBPM"),

    ICRD("ICRD"), TDRC("TDRC"), TDRL("TDRL"), TYER("TYER"), // год выпуска
    INAM("INAM"), TIT2("TIT2"), // Title
    ISFT("ISFT"), TPUB("TPUB"), // Софт/издатель
    IPRD("IPRD"), TALB("TALB"), // Альбом
    IART("IART"), // артист/группа
    IGNR("IGNR"), TCON("TCON"), // жанр
    ITRK("ITRK"), TRCK("TRCK"), // номер трека
    ICMT("ICMT"), COMM("COMM"), // комнтарий
    ICOP("ICOP"), TCOP("TCOP"), // copyright

    ID3_HEAD("ID3 "), TPE1("TPE1"),
    ID3("ID3"),
    TLAN("TLAN"), APIC("APIC"), ISRC("ISRC"),

    TPE2("TPE2"), TCOM("TCOM"), TKEY("TKEY"), TMOO("TMOO"), TPOS("TPOS"),

    UNKNOWN("");

    public static boolean isId3Tag(WavChunkId id) {
        List<WavChunkId> tags = List.of(APIC, TLAN, ID3_HEAD, TPE1, TCOP, TCON, COMM, TRCK, TALB, TPUB, TIT2, TDRC, TDRL, TBPM);
        return tags.contains(id);
    }


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
        return chunk;
    }
}