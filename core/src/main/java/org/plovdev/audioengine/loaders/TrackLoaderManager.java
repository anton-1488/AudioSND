package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.Set;

public interface TrackLoaderManager {
    TrackLoader getTrackLoader();
    Set<TrackFormat> getSupportedFormats();
    TrackExporter getTrackExported();
    TrackEncoder getTrackEncoder();
    TrackDecoder getTrackDecoder();
}