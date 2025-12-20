package org.plovdev.audioengine.loaders.wav.read;

import org.plovdev.audioengine.loaders.ExportUtils;
import org.plovdev.audioengine.loaders.wav.chunks.DataChunk;
import org.plovdev.audioengine.loaders.wav.chunks.FormatChunk;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class WavChunkReader implements AutoCloseable {
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

        WavChunkId chunkId = WavChunkId.fromString(chunkIdStr);

        switch (chunkId) {
            case FORMAT -> {
                return readFormatChunk(body);
            }
            case DATA -> {
                return readDataChunk(body);
            }
            case null -> {
                return new Chunk(WavChunkId.NULL, size, body);
            }
            default -> {
                return new Chunk(chunkId, size, body);
            }
        }
    }

    private FormatChunk readFormatChunk(byte[] body) throws IOException {
        if (body.length < 16) {
            throw new IOException("Некорректный формат fmt chunk");
        }

        int compressionCode = ExportUtils.bytesToInt(body, 0, 2);
        int channels = ExportUtils.bytesToInt(body, 2, 2);
        int sampleRate = ExportUtils.bytesToInt(body, 4, 4);
        int byteRate = ExportUtils.bytesToInt(body, 8, 4);
        int blockAlign = ExportUtils.bytesToInt(body, 12, 2);
        int bitsPerSample = ExportUtils.bytesToInt(body, 14, 2);

        TrackFormat format = getFormat(bitsPerSample, channels, sampleRate);

        return new FormatChunk(format, body.length, body);
    }

    private TrackFormat getFormat(int bitsPerSample, int channels, int sampleRate) throws IOException {
        TrackFormat.AudioCodec codec = switch (bitsPerSample) {
            case 8 -> TrackFormat.AudioCodec.PCM8;
            case 16 -> TrackFormat.AudioCodec.PCM16;
            case 24 -> TrackFormat.AudioCodec.PCM24;
            case 32 -> TrackFormat.AudioCodec.PCM32;
            default -> throw new IOException("Неподдерживаемый битрейт: " + bitsPerSample);
        };

        return new TrackFormat(
                "wav",
                channels,
                bitsPerSample,
                sampleRate,
                true,
                ByteOrder.LITTLE_ENDIAN,
                codec
        );
    }

    private DataChunk readDataChunk(byte[] body) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(body.length);
        buffer.put(body);
        buffer.flip();
        return new DataChunk(buffer);
    }

    public void validateRiffHeader() throws IOException {
        String riff = readString(4);
        if (!"RIFF".equals(riff)) throw new IOException("Файл не WAV");
        int size = readInt(4);
        String wave = readString(4);
        if (!"WAVE".equals(wave)) throw new IOException("Не поддерживается тип файла");
    }

    private int readInt(int size) throws IOException {
        byte[] bytes = new byte[size];
        int r = inputStream.read(bytes);
        if (r != size) throw new IOException("Недостаточно данных для чтения int");
        return ExportUtils.bytesToInt(bytes, 0, size);
    }

    private String readString(int size) throws IOException {
        byte[] bytes = new byte[size];
        int r = inputStream.read(bytes);
        if (r != size) throw new IOException("Недостаточно данных для чтения строки");
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}