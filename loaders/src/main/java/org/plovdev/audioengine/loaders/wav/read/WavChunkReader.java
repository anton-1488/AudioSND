package org.plovdev.audioengine.loaders.wav.read;

import org.plovdev.audioengine.loaders.ExportUtils;
import org.plovdev.audioengine.loaders.wav.ChunkParsersMap;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.plovdev.audioengine.loaders.wav.read.ParseUtils.readInt;
import static org.plovdev.audioengine.loaders.wav.read.ParseUtils.readString;

public class WavChunkReader implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(WavChunkReader.class);
    private final BufferedInputStream inputStream;

    public WavChunkReader(InputStream stream) {
        inputStream = new BufferedInputStream(stream);
    }

    /**
     * Чтение заголовка следующего чанка (ID + размер) и его тела.
     * Возвращает Chunk с данными.
     */
    public Chunk readNextChunk() throws IOException {
        byte[] chunkIdBytes = new byte[4];
        int read = inputStream.read(chunkIdBytes);
        if (read != 4) return null; // конец файла

        String chunkIdStr = new String(chunkIdBytes, StandardCharsets.ISO_8859_1);
        if (chunkIdStr.trim().isEmpty()) return null;

        byte[] sizeBytes = new byte[4];
        read = inputStream.read(sizeBytes);
        if (read != 4) throw new IOException("Не удалось прочитать размер чанка");

        int size = ExportUtils.bytesToInt(sizeBytes, 0, 4);

        // Читаем тело чанка полностью
        byte[] body = new byte[size];
        int totalRead = 0;
        while (totalRead < size) {
            int r = inputStream.read(body, totalRead, size - totalRead);
            if (r == -1) throw new IOException("Недостаточно данных в чанке");
            totalRead += r;
        }

        skipPaddingByte(size);

        WavChunkId chunkId = WavChunkId.fromString(chunkIdStr);
        WavChunkParser parser = ChunkParsersMap.getParser(chunkId);

        if (parser != null) {
            return parser.parse(body);
        } else {
            return new Chunk(WavChunkId.UNKNOWN, size, body);
        }
    }

    public void validateRiffHeader() throws IOException {
        String riff = readString(inputStream, 4);
        if (!"RIFF".equals(riff)) throw new IOException("Файл не WAV");
        int size = readInt(inputStream,4);
        String wave = readString(inputStream,4);
        if (!"WAVE".equals(wave)) throw new IOException("Не поддерживается тип файла");
    }

    private void skipPaddingByte(int chunkSize) throws IOException {
        if ((chunkSize & 1) != 0) {
            int padding = inputStream.read();
        }
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}