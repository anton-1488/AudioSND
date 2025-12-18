package org.plovdev.audioengine.devices;

import java.nio.ByteBuffer;

public interface ChunkProvider {
    ByteBuffer onNextChunkRequired(int req);
}