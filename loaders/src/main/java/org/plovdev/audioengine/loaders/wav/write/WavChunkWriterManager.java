package org.plovdev.audioengine.loaders.wav.write;

import org.plovdev.audioengine.loaders.wav.ChunkParsersMap;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.loaders.wav.write.writers.WavChunkWriter;

import java.io.OutputStream;

public class WavChunkWriterManager {
    private final OutputStream outputStream;

    public WavChunkWriterManager(OutputStream stream) {
        this.outputStream = stream;
    }

    public <T> void writeChunk(WavChunkId id, T toWrite) {
        WavChunkWriter<?> writer = ChunkParsersMap.getWriter(id);
        if (writer != null) {
            writeWithTypeCheck(writer, toWrite);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void writeWithTypeCheck(WavChunkWriter<?> writer, T toWrite) {
        if (writer.getType().isInstance(toWrite)) {
            WavChunkWriter<T> typedWriter = (WavChunkWriter<T>) writer;
            typedWriter.write(outputStream, toWrite);
        } else {
            throw new IllegalArgumentException("Type mismatch: expected " + writer.getType() + ", got " + toWrite.getClass());
        }
    }
}