package org.plovdev.audioengine.loaders;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.format.TrackFormat;

/**
 * Central management point for track loading and processing.
 * <p>
 * Provides access to:
 * <ul>
 *   <li>{@link TrackLoader} - loads raw audio data</li>
 *   <li>{@link TrackDecoder} - decodes compressed formats to PCM</li>
 *   <li>{@link TrackEncoder} - encodes PCM to compressed formats</li>
 *   <li>{@link TrackExporter} - writes tracks to output streams</li>
 * </ul>
 * </p>
 *
 * @see Track
 * @see TrackFormat
 * @see PathLocator
 * @author Anton
 * @version 1.0
 * @since 1.0
 */
public interface TrackLoaderManager {
    @NotNull TrackLoader getTrackLoader();
    @NotNull TrackExporter getTrackExporter();
    @NotNull TrackEncoder getTrackEncoder();
    @NotNull TrackDecoder getTrackDecoder();

    void registerPathLocator(@NotNull PathLocator locator);
}