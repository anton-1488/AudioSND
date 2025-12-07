package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.utils.format.TrackFormat;
import org.plovdev.audioengine.utils.meta.MetadataUtils;
import org.plovdev.audioengine.utils.meta.TrackMetaData;

import java.nio.ByteBuffer;
import java.time.Duration;

public class Track {
    private final ByteBuffer trackData;
    private final Duration duration;
    private final TrackFormat format;
    private TrackMetaData metaData;

    public Track(ByteBuffer trackData, Duration duration, TrackFormat format, TrackMetaData metaData) {
        this.trackData = trackData;
        this.duration = duration;
        this.format = format;
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
        return new Track(trackData.duplicate(), Duration.ofMillis(duration.toMillis()), format, MetadataUtils.merge(metaData));
    }
}