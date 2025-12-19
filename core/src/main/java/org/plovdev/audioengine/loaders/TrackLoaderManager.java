package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

/**
 * Node of track load managing.
 * Provide all tool for load/decode track
 *
 * @see Track
 * @see TrackLoader
 * @see TrackFormat
 * @see TrackExporter
 * @see TrackEncoder
 * @see TrackDecoder
 *
 * @author Anton
 * @version 1.0
 */
public interface TrackLoaderManager {
    TrackLoader getTrackLoader();
    TrackExporter getTrackExporter();
    TrackEncoder getTrackEncoder();
    TrackDecoder getTrackDecoder();

    void registerPathLocator(PathLocator locator);
}