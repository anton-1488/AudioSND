package main.java.org.plovdev.audioengine;

import main.java.org.plovdev.audioengine.exceptions.TrackLoadException;
import main.java.org.plovdev.audioengine.loaders.TrackLoader;
import main.java.org.plovdev.audioengine.mixer.TrackMixer;
import main.java.org.plovdev.audioengine.tracks.TrackPlayer;
import main.java.org.plovdev.audioengine.tracks.Track;
import main.java.org.plovdev.audioengine.utils.AudioEngineConfig;

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