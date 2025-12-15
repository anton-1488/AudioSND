package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface TrackLoader {
    Track loadTrack(String path) throws TrackLoadException;
    Track loadTrack(InputStream stream) throws TrackLoadException;
    Track loadTrack(URI uri) throws TrackLoadException;

    List<Track> batchLoadTrack(String... path) throws TrackLoadException;
    List<Track> batchLoadTrack(InputStream... stream) throws TrackLoadException;
    List<Track> batchLoadTrack(URI... uri) throws TrackLoadException;

    TrackMetadata readTrackMetadata(String src);
    TrackMetadata readTrackMetadata(InputStream src);
    TrackMetadata readTrackMetadata(URI src);

    TrackFormat getTrackFormat(String src);
    TrackFormat getTrackFormat(InputStream src);
    TrackFormat getTrackFormat(URI src);

    boolean isSupported(String filename);
    boolean isSupported(InputStream stream);
    boolean isSupported(URI uri);

    boolean isSupporteds(String... filename);
    boolean isSupporteds(InputStream... stream);
    boolean isSupporteds(URI... uri);

    void setLoadListener(LoadListener listener);
    LoadListener getLoadListener();
}