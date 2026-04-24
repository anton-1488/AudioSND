package org.plovdev.audioengine.loaders.io;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.api.Track;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class TrackOutputStream implements AutoCloseable {
    protected final Track track;
    protected final OutputStream toWrite;

    public TrackOutputStream(Track track, OutputStream toWrite) {
        this.track = track;
        this.toWrite = toWrite;
    }

    protected abstract void writeHead() throws IOException;

    protected abstract void writeEnd() throws IOException;

    public void write(@NotNull ByteBuffer body) throws IOException {
        body.position(0);

        byte[] bytes = new byte[body.capacity()];
        body.get(bytes);
        toWrite.write(bytes);
    }

    @Override
    public void close() throws IOException {
        writeEnd();
        toWrite.close();
    }
}