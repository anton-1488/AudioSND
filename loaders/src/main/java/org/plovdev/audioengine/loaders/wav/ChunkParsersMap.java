package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.DataChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.FormatChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.ListChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkParsersMap {
    private static final Map<WavChunkId, WavChunkParser> parsers = new ConcurrentHashMap<>();
    static {
        parsers.put(WavChunkId.FMT, new FormatChunkParser());
        parsers.put(WavChunkId.DATA, new DataChunkParser());
        parsers.put(WavChunkId.LIST, new ListChunkParser());
    }
    public static WavChunkParser getParser(WavChunkId id) {
        return parsers.get(id);
    }
}