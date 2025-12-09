package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.io.OutputStream;

public interface TrackExporter {
    void save(Track track, OutputStream outputStream);
    void export(Track track, OutputStream output, TrackFormat toFormat);
}