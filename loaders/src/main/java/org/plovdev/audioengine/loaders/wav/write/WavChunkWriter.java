package org.plovdev.audioengine.loaders.wav.write;

import org.plovdev.audioengine.exceptions.TrackExportException;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import static org.plovdev.audioengine.loaders.ExportUtils.intToLittleEndian;
import static org.plovdev.audioengine.loaders.ExportUtils.shortToLittleEndian;
import static org.plovdev.audioengine.loaders.wav.struct.WavChunkId.*;

public class WavChunkWriter {
    private final OutputStream outputStream;

    public WavChunkWriter(OutputStream stream) {
        this.outputStream = stream;
    }

    public void writeFormat(TrackFormat format) {
        try {
            int channels = format.channels();
            int sampleRate = format.sampleRate();
            int bitDepth = format.bitsPerSample();
            boolean floatFormat = bitDepth == 32;

            int blockAlign = channels * (bitDepth / 8);
            int byteRate = sampleRate * blockAlign;

            outputStream.write(FORMAT.getChunk().getBytes());
            outputStream.write(intToLittleEndian(16));

            outputStream.write(shortToLittleEndian((short) (floatFormat ? 3 : 1))); // PCM=1, float=3
            outputStream.write(shortToLittleEndian((short) channels));
            outputStream.write(intToLittleEndian(sampleRate));
            outputStream.write(intToLittleEndian(byteRate));
            outputStream.write(shortToLittleEndian((short) blockAlign));
            outputStream.write(shortToLittleEndian((short) bitDepth));
        } catch (Exception e) {
            throw new TrackExportException(e.getMessage());
        }
    }

    public void writeDataChunk(ByteBuffer trackData, TrackFormat format) {
        try {
            int bitDepth = format.bitsPerSample();
            int dataChunkSize = trackData.remaining();

            outputStream.write(DATA.getChunk().getBytes());
            outputStream.write(intToLittleEndian(dataChunkSize));

            byte[] bytes = new byte[dataChunkSize];
            trackData.get(bytes);

            outputStream.write(bytes);
        } catch (Exception e) {
            throw new TrackExportException(e.getMessage());
        }
    }
}