package org.plovdev.audioengine.loaders.wav.chunks;

import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

public class APICStructure extends Chunk {
    private int encodeing;
    private String mimeType;
    private int picType;
    private String desc;

    public APICStructure(WavChunkId chunk, int size, byte[] bytes, int encodeing, String mimeType, int picType, String desc) {
        super(chunk, size, bytes);
        this.encodeing = encodeing;
        this.mimeType = mimeType;
        this.picType = picType;
        this.desc = desc;
    }

    public int getEncodeing() {
        return encodeing;
    }

    public void setEncodeing(int encodeing) {
        this.encodeing = encodeing;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getPicType() {
        return picType;
    }

    public void setPicType(int picType) {
        this.picType = picType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}