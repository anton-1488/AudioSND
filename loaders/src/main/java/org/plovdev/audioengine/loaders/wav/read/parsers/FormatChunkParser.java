package org.plovdev.audioengine.loaders.wav.read.parsers;

import org.plovdev.audioengine.loaders.ExportUtils;
import org.plovdev.audioengine.loaders.wav.chunks.FormatChunk;
import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.format.TrackFormat;

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

        TrackFormat format = getFormat(compressionCode, bitsPerSample, channels, sampleRate);

        return new FormatChunk(format, body.length, body);
    }

    private TrackFormat getFormat(int code, int bitsPerSample, int channels, int sampleRate) {
        TrackFormat.AudioCodec codec;
        boolean signed = true;
        ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

        switch (code) {
            case 1:
                codec = switch (bitsPerSample) {
                    case 8 -> {
                        signed = false;
                        yield TrackFormat.AudioCodec.PCM8;
                    }
                    case 16 -> TrackFormat.AudioCodec.PCM16;
                    case 20, 24 -> TrackFormat.AudioCodec.PCM24;
                    case 32 -> TrackFormat.AudioCodec.PCM32;
                    default -> throw new IllegalArgumentException(
                            "Неподдерживаемый битрейт для PCM: " + bitsPerSample
                    );
                };
                break;

            case 3:
                if (bitsPerSample == 32) {
                    codec = TrackFormat.AudioCodec.FLOAT32;
                } else if (bitsPerSample == 64) {
                    codec = TrackFormat.AudioCodec.FLOAT64;
                } else {
                    throw new IllegalArgumentException(
                            "IEEE Float поддерживает только 32 или 64-bit: " + bitsPerSample
                    );
                }
                break;

            case 6:
                if (bitsPerSample == 8) {
                    codec = TrackFormat.AudioCodec.ALAW;
                    signed = false;
                } else {
                    throw new IllegalArgumentException("A-law поддерживает только 8-bit");
                }
                break;

            case 7:
                if (bitsPerSample == 8) {
                    codec = TrackFormat.AudioCodec.ULAW;
                    signed = false;
                } else {
                    throw new IllegalArgumentException("μ-law поддерживает только 8-bit");
                }
                break;

            case 0x0011:
                codec = TrackFormat.AudioCodec.IMA_ADPCM;
                break;

            case 0x0055:
                codec = TrackFormat.AudioCodec.MP3;
                break;

            default:
                throw new IllegalArgumentException(
                        String.format("Неподдерживаемый compression code: 0x%04X (%d)", code, code)
                );
        }

        return new TrackFormat(
                channels,
                bitsPerSample,
                sampleRate,
                signed,
                byteOrder,
                codec
        );
    }
}