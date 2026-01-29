package org.plovdev.audioengine.loaders.wav.read.parsers;

import org.plovdev.audioengine.loaders.ExportUtils;
import org.plovdev.audioengine.loaders.wav.chunks.FormatChunk;
import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

public class FormatChunkParser implements WavChunkParser {
    @Override
    public boolean canParse(WavChunkId id) {
        return id == WavChunkId.FMT;
    }

    @Override
    public Chunk parse(byte[] body) {
        if (body.length < 16) {
            throw new IllegalArgumentException("Некорректный формат fmt chunk");
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

    private TrackFormat getFormat(int bitsPerSample, int channels, int sampleRate) {
        TrackFormat.AudioCodec codec = switch (bitsPerSample) {
            case 8 -> TrackFormat.AudioCodec.PCM8;
            case 16 -> TrackFormat.AudioCodec.PCM16;
            case 20, 24 -> TrackFormat.AudioCodec.PCM24;
            case 32 -> TrackFormat.AudioCodec.PCM32;
            default -> throw new IllegalArgumentException("Неподдерживаемый битрейт: " + bitsPerSample);
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
}