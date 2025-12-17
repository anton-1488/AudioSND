package org.plovdev.audioengine.wav;

import org.plovdev.audioengine.loaders.*;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.Set;

public class WavTrackLoaderManager implements TrackLoaderManager {
    @Override
    public TrackLoader getTrackLoader() {
        return new WavTrackLoader();
    }

    @Override
    public Set<TrackFormat> getSupportedFormats() {
        return Set.of();
    }

    @Override
    public TrackExporter getTrackExported() {
        return null;
    }

    @Override
    public TrackEncoder getTrackEncoder() {
        return null;
    }

    @Override
    public TrackDecoder getTrackDecoder() {
        return null;
    }
}
