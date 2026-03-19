package org.plovdev.audioengine.loaders.flac;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.exceptions.loaders.TrackLoadException;
import org.plovdev.audioengine.format.TrackFormat;
import org.plovdev.audioengine.loaders.TrackDecoder;

public class FlacTrackDecoder implements TrackDecoder {
    /**
     * Decodes the input track to the specified PCM format.
     * <p>
     * Creates a new track instance. Original track is not modified.
     * Output format must be a PCM format supported by the decoder.
     * </p>
     *
     * @param input     source track (compressed or non-PCM)
     * @param outFormat target PCM format (sample rate, channels, bit depth)
     * @return new track containing decoded PCM audio data
     * @throws TrackLoadException if decoding fails or output format is not supported
     */
    @Override
    public @NotNull Track decode(@NotNull Track input, @NotNull TrackFormat outFormat) {
        return null;
    }
}