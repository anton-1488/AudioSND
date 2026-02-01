package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.DataChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.FormatChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.ID3ChunkParser;
import org.plovdev.audioengine.loaders.wav.read.parsers.ListChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.loaders.wav.write.writers.DataChunkWriter;
import org.plovdev.audioengine.loaders.wav.write.writers.FormatChunkWriter;
import org.plovdev.audioengine.loaders.wav.write.writers.ID3ChunkWriter;
import org.plovdev.audioengine.loaders.wav.write.writers.WavChunkWriter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkParsersMap {
    private static final Map<WavChunkId, WavChunkParser> parsers = new ConcurrentHashMap<>();
    private static final Map<WavChunkId, WavChunkWriter<?>> writers = new ConcurrentHashMap<>();
    static {
        parsers.put(WavChunkId.FMT, new FormatChunkParser());
        parsers.put(WavChunkId.DATA, new DataChunkParser());
        parsers.put(WavChunkId.LIST, new ListChunkParser());
        parsers.put(WavChunkId.ID3_HEAD, new ID3ChunkParser());

        writers.put(WavChunkId.FMT, new FormatChunkWriter());
        writers.put(WavChunkId.DATA, new DataChunkWriter());
        writers.put(WavChunkId.ID3_HEAD, new ID3ChunkWriter());
    }
    public static WavChunkParser getParser(WavChunkId id) {
        return parsers.get(id);
    }

    public static WavChunkWriter<?> getWriter(WavChunkId id) {
        return writers.get(id);
    }
}