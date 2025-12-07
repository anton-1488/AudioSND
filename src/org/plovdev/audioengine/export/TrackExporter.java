package org.plovdev.audioengine.export;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.utils.format.TrackFormat;

import java.io.OutputStream;

public interface TrackExporter {
    void export(Track track, OutputStream outputStream);

    void export(Track track, TrackFormat format, OutputStream output);
}