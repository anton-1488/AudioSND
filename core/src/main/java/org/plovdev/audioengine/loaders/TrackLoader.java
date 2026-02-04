package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.exceptions.loaders.TrackLoadException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

public interface TrackLoader {
    Track loadTrack(File file) throws TrackLoadException;
    Track loadTrack(InputStream stream) throws TrackLoadException;
    Track loadTrack(URI uri) throws TrackLoadException;

    TrackMetadata readTrackMetadata(File src);
    TrackMetadata readTrackMetadata(InputStream src);
    TrackMetadata readTrackMetadata(URI src);

    TrackFormat getTrackFormat(File src);
    TrackFormat getTrackFormat(InputStream src);
    TrackFormat getTrackFormat(URI src);

    boolean isSupported(File file);
    boolean isSupported(TrackFormat format);
    boolean isSupported(InputStream stream);
    boolean isSupported(URI uri);

    void setLoadListener(LoadListener listener);
    LoadListener getLoadListener();
}