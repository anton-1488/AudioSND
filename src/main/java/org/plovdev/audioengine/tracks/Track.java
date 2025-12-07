package main.java.org.plovdev.audioengine.tracks;

import main.java.org.plovdev.audioengine.tracks.format.TrackFormat;
import main.java.org.plovdev.audioengine.tracks.meta.MetadataUtils;
import main.java.org.plovdev.audioengine.tracks.meta.TrackMetaData;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

public class Track {
    private final ByteBuffer trackData;
    private final Duration duration;
    private final TrackFormat format;
    private TrackMetaData metaData;

    public Track(ByteBuffer trackData, Duration duration, TrackFormat format, TrackMetaData metaData) {
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
    public TrackMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(TrackMetaData metaData) {
        this.metaData = metaData;
    }

    public Track cloneTrack() {
        TrackFormat copiedFprmat = new TrackFormat(format.extension(), format.channels(), format.bitsPerSample(), format.sampleRate(), format.signed(), format.byteOrder());
        return new Track(trackData.duplicate(), Duration.ofMillis(duration.toMillis()), copiedFprmat, MetadataUtils.merge(metaData));
    }
}