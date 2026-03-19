package org.plovdev.audioengine.loaders.flac;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.exceptions.loaders.TrackLoadException;
import org.plovdev.audioengine.format.TrackFormat;
import org.plovdev.audioengine.loaders.TrackEncoder;

public class FlacTrackEncoder implements TrackEncoder {
    /**
     * Encodes the input PCM track to the specified output format.
     * <p>
     * Creates a new track instance. Original track is not modified.
     * Input track must be in PCM format.
     * </p>
     *
     * @param input     source track (PCM audio)
     * @param outFormat target format (e.g., MP3, AAC, FLAC)
     * @return new track containing encoded audio data
     * @throws TrackLoadException if encoding fails, input is not PCM, or output format is not supported
     */
    @Override
    public @NotNull Track encode(@NotNull Track input, @NotNull TrackFormat outFormat) {
        return null;
    }
}