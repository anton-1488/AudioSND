package org.plovdev.audioengine.loaders.wav.chunks;

import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.tracks.format.TrackFormat;

public class FormatChunk extends Chunk {
    private TrackFormat.AudioCodec audioCodec;
    private int channels;
    private int sampleRate;
    private int byteRate;
    private int blockAlign;
    private int bitPerSample;
    private int extraBytes;
    private TrackFormat format;

    public FormatChunk(TrackFormat format, int size, byte[] bytes) {
        super(WavChunkId.FORMAT, size, bytes);

        this.audioCodec = format.audioCodec();
        this.channels = format.channels();
        this.sampleRate = format.sampleRate();
        this.byteRate = sampleRate * chunkSize;
        this.bitPerSample = format.bitsPerSample();
        this.blockAlign = (bitPerSample / 8) * channels;
        this.format = format;
    }

    public TrackFormat.AudioCodec getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(TrackFormat.AudioCodec audioCodec) {
        this.audioCodec = audioCodec;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getByteRate() {
        return byteRate;
    }

    public void setByteRate(int byteRate) {
        this.byteRate = byteRate;
    }

    public int getBlockAlign() {
        return blockAlign;
    }

    public void setBlockAlign(int blockAlign) {
        this.blockAlign = blockAlign;
    }

    public int getBitPerSample() {
        return bitPerSample;
    }

    public void setBitPerSample(int bitPerSample) {
        this.bitPerSample = bitPerSample;
    }

    public int getExtraBytes() {
        return extraBytes;
    }

    public void setExtraBytes(int extraBytes) {
        this.extraBytes = extraBytes;
    }

    public TrackFormat getFormat() {
        return format;
    }

    public void setFormat(TrackFormat format) {
        this.format = format;
    }
}