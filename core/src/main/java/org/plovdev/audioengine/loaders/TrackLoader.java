package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.io.InputStream;
import java.net.URI;

public interface TrackLoader {
    Track loadTrack(String path) throws TrackLoadException;
    Track loadTrack(InputStream stream) throws TrackLoadException;
    Track loadTrack(URI uri) throws TrackLoadException;

    TrackMetadata readTrackMetadata(String src);
    TrackMetadata readTrackMetadata(InputStream src);
    TrackMetadata readTrackMetadata(URI src);

    TrackFormat getTrackFormat(String src);
    TrackFormat getTrackFormat(InputStream src);
    TrackFormat getTrackFormat(URI src);

    boolean isSupported(String filename);
    boolean isSupported(InputStream stream);
    boolean isSupported(URI uri);

    void setLoadListener(LoadListener listener);
    LoadListener getLoadListener();
}