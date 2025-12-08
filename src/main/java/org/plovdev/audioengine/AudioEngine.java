package org.plovdev.audioengine;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.loaders.TrackLoader;
import org.plovdev.audioengine.mixer.TrackMixer;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.utils.AudioEngineConfig;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface AudioEngine extends AutoCloseable {
    void init();
    void init(AudioEngineConfig config);

    Track loadTrack(String path) throws TrackLoadException;
    Track loadTrack(InputStream stream) throws TrackLoadException;
    Track loadTrack(URI uri) throws TrackLoadException;

    List<Track> batchLoadTrack(String... path) throws TrackLoadException;
    List<Track> batchLoadTrack(InputStream... stream) throws TrackLoadException;
    List<Track> batchLoadTrack(URI... uri) throws TrackLoadException;

    TrackMixer getMixer();
    TrackPlayer getTrackPlayer(Track track);

    void addLoader(TrackLoader loader);
    void removeLoader(TrackLoader loader);
    List<TrackLoader> getAvailableLoaders();

    @Override
    void close();
}