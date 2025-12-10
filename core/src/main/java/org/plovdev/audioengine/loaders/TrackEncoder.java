package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

public interface TrackEncoder {
    Track encodeFromPCM(Track input);
    Track encodeFromFormat(Track input, TrackFormat outFormat);
}