package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.loaders.*;
import org.plovdev.audioengine.loaders.wav.write.WavTrackExporter;

public class WavTrackLoaderManager implements TrackLoaderManager {
    private final WavTrackLoader loader;
    private final WavTrackExporter exporter;
    private final WavTrackEncoder encoder;
    private final WavTrackDecoder decoder;

    public WavTrackLoaderManager() {
        loader = new WavTrackLoader();
        exporter = new WavTrackExporter();
        encoder = new WavTrackEncoder();
        decoder = new WavTrackDecoder();
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
        return encoder;
    }

    @Override
    public TrackDecoder getTrackDecoder() {
        return decoder;
    }

    @Override
    public void registerPathLocator(PathLocator locator) {
        loader.addLoactor(locator);
    }
}
