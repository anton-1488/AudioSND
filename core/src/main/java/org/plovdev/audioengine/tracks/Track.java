package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.exceptions.AudioEngineException;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

/**
 * Most important AudioSND class.
 * The class is the fundamental unit of work with the engine.
 *
 * @author Anton
 * @version 1.0
 */
public class Track {
    // Track information
    private final ByteBuffer trackData;
    private final Duration duration;
    private final TrackFormat format;
    private TrackMetadata metaData;

    /**
     * Create audio track with DIRECT ByteBuffer.
     *
     * @param trackData track's bytes (MUST be direct ByteBuffer)
     * @param duration  audio duration
     * @param format    audio track format
     * @param metaData  metadata, loaded from file, or created by hands
     * @throws NullPointerException     if trackData or format is null
     * @throws AudioEngineException if trackData is not a direct buffer
     */
    public Track(ByteBuffer trackData, Duration duration, TrackFormat format, TrackMetadata metaData) {
        Objects.requireNonNull(trackData, "trackData must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(format, "format must not be null");

        if (!trackData.isDirect()) {
            throw new AudioEngineException("Track data must be allocated with ByteBuffer.allocateDirect() for native audio processing");
        }

        this.trackData = trackData.asReadOnlyBuffer(); // Read-only wrapper
        this.duration = duration;
        this.format = format;
        this.metaData = metaData; // Can be null
    }

    public ByteBuffer getTrackData() {
        return trackData;
    }

    public Duration getDuration() {
        return duration;
    }

    public TrackFormat getFormat() {
        return format;
    }

    public TrackMetadata getMetaData() {
        return metaData;
    }

    /**
     * Able set metadata to export track.
     *
     * @param metaData new track metadata.
     */
    public void setMetaData(TrackMetadata metaData) {
        this.metaData = metaData;
    }
}