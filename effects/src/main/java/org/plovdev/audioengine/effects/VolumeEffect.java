package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.utils.TrackUtils;

import java.nio.ByteBuffer;

public class VolumeEffect implements AudioEffect {
    private float volume = 0.5f;

    public VolumeEffect() {
    }

    public VolumeEffect(float volume) {
        this.volume = volume;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public Track apply(Track source) {
        byte[] bytes = TrackUtils.getTrackBytes(source);

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] * volume);
        }

        ByteBuffer ready = ByteBuffer.allocateDirect(bytes.length);
        ready.put(bytes);
        ready.flip();

        return new Track(ready, source.getDuration(), source.getFormat(), source.getMetaData());
    }
}