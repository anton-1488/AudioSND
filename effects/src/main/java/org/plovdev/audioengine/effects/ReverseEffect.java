package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;

public class ReverseEffect implements AudioEffect {
    public ReverseEffect() {
    }

    @Override
    public ByteBuffer process(TrackFormat format, ByteBuffer source) {
        int frameSize = (format.bitDepth() / 8) * format.channels();
        int size = source.limit();
        ByteBuffer processed = ByteBuffer.allocateDirect(size);
        byte[] chunk = new byte[frameSize];

        for (int i = size - frameSize; i >= 0; i -= frameSize) {
            source.position(i);
            source.get(chunk);
            processed.put(chunk);
        }

        processed.flip();
        return processed;
    }
}