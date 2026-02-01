package org.plovdev.audioengine.loaders.wav.write.writers;

import java.io.OutputStream;

public interface WavChunkWriter<T> {
    Class<?> getType();
    void write(OutputStream stream, T writeObject);
}