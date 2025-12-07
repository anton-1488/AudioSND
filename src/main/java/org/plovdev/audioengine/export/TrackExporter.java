package main.java.org.plovdev.audioengine.export;

import main.java.org.plovdev.audioengine.tracks.Track;
import main.java.org.plovdev.audioengine.tracks.format.TrackFormat;

import java.io.OutputStream;

public interface TrackExporter {
    void export(Track track, OutputStream outputStream);

    void export(Track track, TrackFormat format, OutputStream output);
}