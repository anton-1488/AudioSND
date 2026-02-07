package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

public interface TrackDecoder {
    Track decode(Track input, TrackFormat outFormat);
}