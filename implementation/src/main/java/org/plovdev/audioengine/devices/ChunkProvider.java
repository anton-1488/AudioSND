package org.plovdev.audioengine.devices;

public interface ChunkProvider {
    void onNextChunkRequired(int req);
}