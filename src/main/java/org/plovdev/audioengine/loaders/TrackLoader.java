package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetaData;

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

    TrackMetaData readTrackMetadata(String src);
    TrackMetaData readTrackMetadata(InputStream src);
    TrackMetaData readTrackMetadata(URI src);

    TrackFormat getTackFormat(String src);
    TrackFormat getTackFormat(InputStream src);
    TrackFormat getTackFormat(URI src);

    boolean isSupported(String filename);
    boolean isSupported(InputStream stream);
    boolean isSupported(URI uri);

    void setLoadListener(LoadListener listener);
    LoadListener getLoadListener();
}