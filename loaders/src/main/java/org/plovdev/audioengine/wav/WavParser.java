package org.plovdev.audioengine.wav;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavParser {
    private final byte[] data;
    private final ByteBuffer buffer;

    private int sampleRate;
    private int channels;
    private int bitDepth;
    private long dataSize;
    private int dataOffset;

    public WavParser(byte[] wavData) {
        this.data = wavData;
        this.buffer = ByteBuffer.wrap(wavData).order(ByteOrder.LITTLE_ENDIAN);
    }

    public void parseHeader() throws IllegalArgumentException {
        // Проверяем RIFF signature
        if (data.length < 12 || !"RIFF".equals(readString(0, 4))) {
            throw new IllegalArgumentException("Not a valid WAV file");
        }

        // Проверяем WAVE signature
        if (!"WAVE".equals(readString(8, 4))) {
            throw new IllegalArgumentException("Not a valid WAV file");
        }

        // Ищем fmt chunk
        int chunkOffset = 12;
        boolean foundFmt = false;

        while (chunkOffset < data.length - 8) {
            String chunkId = readString(chunkOffset, 4);
            int chunkSize = buffer.getInt(chunkOffset + 4);

            if ("fmt ".equals(chunkId)) {
                parseFormatChunk(chunkOffset + 8, chunkSize);
                foundFmt = true;
                break;
            }

            chunkOffset += 8 + chunkSize;
        }

        if (!foundFmt) {
            throw new IllegalArgumentException("fmt chunk not found");
        }

        // Ищем data chunk
        chunkOffset = 36; // После fmt chunk
        boolean foundData = false;

        while (chunkOffset < data.length - 8) {
            String chunkId = readString(chunkOffset, 4);
            int chunkSize = buffer.getInt(chunkOffset + 4);

            if ("data".equals(chunkId)) {
                this.dataSize = chunkSize;
                this.dataOffset = chunkOffset + 8;
                foundData = true;
                break;
            }

            chunkOffset += 8 + chunkSize;
        }

        if (!foundData) {
            throw new IllegalArgumentException("data chunk not found");
        }
    }

    private void parseFormatChunk(int offset, int chunkSize) {
        int audioFormat = buffer.getShort(offset) & 0xFFFF;
        if (audioFormat != 1) {
            throw new IllegalArgumentException("Only PCM format supported, got: " + audioFormat);
        }

        this.channels = buffer.getShort(offset + 2) & 0xFFFF;
        this.sampleRate = buffer.getInt(offset + 4);
        this.bitDepth = buffer.getShort(offset + 14) & 0xFFFF;
    }

    public byte[] getAudioData() {
        byte[] audioData = new byte[(int) dataSize];
        System.arraycopy(data, dataOffset, audioData, 0, (int) dataSize);
        return audioData;
    }

    public ByteBuffer getAudioDataAsBuffer() {
        return ByteBuffer.wrap(getAudioData()).order(ByteOrder.LITTLE_ENDIAN);
    }

    private String readString(int offset, int length) {
        return new String(data, offset, length);
    }

    // Getters
    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getNumSamples() {
        return dataSize / ((long) channels * (bitDepth / 8));
    }

    public int getDataOffset() {
        return dataOffset;
    }
}