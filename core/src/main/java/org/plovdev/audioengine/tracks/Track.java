package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

public class Track {
    private final ByteBuffer trackData;
    private final Duration duration;
    private final TrackFormat format;
    private TrackMetadata metaData;

    public Track(ByteBuffer trackData, Duration duration, TrackFormat format, TrackMetadata metaData) {
        this.trackData = Objects.requireNonNull(trackData);
        this.duration = duration;
        this.format = Objects.requireNonNull(format);
        this.metaData = metaData;
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

    public void setMetaData(TrackMetadata metaData) {
        this.metaData = metaData;
    }

    public Track cloneTrack() {
        TrackFormat copiedFprmat = new TrackFormat(format.extension(), format.channels(), format.bitsPerSample(), format.sampleRate(), format.signed(), format.byteOrder());
        return new Track(trackData.duplicate(), Duration.ofMillis(duration.toMillis()), copiedFprmat, metaData);
    }
}