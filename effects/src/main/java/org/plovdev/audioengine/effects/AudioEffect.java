package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.Track;

public interface AudioEffect {
    Track apply(Track source);
}