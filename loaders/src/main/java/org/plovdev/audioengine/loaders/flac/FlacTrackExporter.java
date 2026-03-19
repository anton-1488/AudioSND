package org.plovdev.audioengine.loaders.flac;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.exceptions.loaders.TrackExportException;
import org.plovdev.audioengine.loaders.TrackExporter;

import java.io.OutputStream;

public class FlacTrackExporter implements TrackExporter {
    /**
     * Writes the track audio data to the output stream.
     * <p>
     * The output format is implementation-specific and should be
     * documented by concrete implementations.
     * Original track is not modified.
     * </p>
     *
     * @param track        source track to export
     * @param outputStream destination stream (not closed by this method)
     * @throws TrackExportException if export operation fails
     * @throws NullPointerException if any argument is null
     */
    @Override
    public void save(@NotNull Track track, @NotNull OutputStream outputStream) {

    }
}