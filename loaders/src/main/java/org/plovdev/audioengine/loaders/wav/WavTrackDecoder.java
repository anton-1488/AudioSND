package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.loaders.TrackDecoder;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

public class WavTrackDecoder implements TrackDecoder {
    @Override
    public Track decodeToPCM(Track input) {
        return null;
    }

    @Override
    public Track decodeToFormat(Track input, TrackFormat outFormat) {
        return null;
    }
}