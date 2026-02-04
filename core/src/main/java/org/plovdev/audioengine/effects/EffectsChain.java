package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EffectsChain {
    private final List<AudioEffect> effects = new ArrayList<>();

    public EffectsChain() {
    }

    public EffectsChain addEffect(AudioEffect effect) {
        Objects.requireNonNull(effect);
        effects.add(effect);
        return this;
    }
    public EffectsChain removeEffect(AudioEffect effect) {
        Objects.requireNonNull(effect);
        effects.remove(effect);
        return this;
    }

    public Track apply(Track source) {
        if (source == null) {
            throw new IllegalArgumentException("Source track cannot be null");
        }
        if (effects.isEmpty()) {
            return source;
        }

        ByteBuffer original = source.getTrackData();
        TrackFormat format = source.getFormat();

        for (AudioEffect effect : effects) {
            effect.setup(format);
            original = effect.process(original);
        }

        return new Track(original, source.getDuration(), format, source.getMetaData());
    }
}