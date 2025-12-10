package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.tracks.Track;

import java.io.OutputStream;

public interface TrackExporter {
    void save(Track track, OutputStream outputStream);
}