package org.plovdev.audioengine.loaders.raw;

import org.plovdev.audioengine.loaders.*;
import org.plovdev.audioengine.format.TrackFormat;

public class RawTrackLoaderManager implements TrackLoaderManager {
    private final RawTrackLoader loader;
    private final RawTrackExporter exporter;

    public RawTrackLoaderManager(TrackFormat format) {
        loader = new RawTrackLoader(format);
        exporter = new RawTrackExporter();
    }

    @Override
    public TrackLoader getTrackLoader() {
        return loader;
    }

    @Override
    public TrackExporter getTrackExporter() {
        return exporter;
    }

    @Override
    public TrackEncoder getTrackEncoder() {
        throw new UnsupportedOperationException("Raw not supports encoder");
    }

    @Override
    public TrackDecoder getTrackDecoder() {
        throw new UnsupportedOperationException("Raw not supports decoder");
    }

    @Override
    public void registerPathLocator(PathLocator locator) {
        loader.addLoactor(locator);
    }
}