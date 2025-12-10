package org.plovdev.audioengine;

import org.plovdev.audioengine.callback.rx.EventManager;
import org.plovdev.audioengine.devices.InputAudioDevice;
import org.plovdev.audioengine.devices.OutputAudioDevice;
import org.plovdev.audioengine.exceptions.AudioEngineException;
import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.loaders.TrackLoaderManager;
import org.plovdev.audioengine.mixer.TrackMixer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.utils.AudioEngineConfig;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class NativeAudioEngine implements AudioEngine {
    /**
     * Initializes engine with custom configuration.
     *
     * @param config engine configuration
     * @throws IllegalStateException if already initialized
     * @throws AudioEngineException  if initialization fails
     */
    @Override
    public void init(AudioEngineConfig config) throws AudioEngineException {

    }

    /**
     * @return true if engine is initialized and ready
     */
    @Override
    public boolean isInitialized() {
        return false;
    }

    /**
     * @return current engine configuration
     * @throws IllegalStateException if not initialized
     */
    @Override
    public AudioEngineConfig getConfig() {
        return null;
    }

    /**
     * Return the local event manager for listening event into engine.
     *
     * @return local event manager
     */
    @Override
    public EventManager getEventManager() {
        return null;
    }

    @Override
    public Track loadTrack(String path) throws TrackLoadException {
        return null;
    }

    @Override
    public Track loadTrack(InputStream stream) throws TrackLoadException {
        return null;
    }

    @Override
    public Track loadTrack(URI uri) throws TrackLoadException {
        return null;
    }

    @Override
    public List<Track> batchLoadTrack(String... path) throws TrackLoadException {
        return List.of();
    }

    @Override
    public List<Track> batchLoadTrack(InputStream... stream) throws TrackLoadException {
        return List.of();
    }

    @Override
    public List<Track> batchLoadTrack(URI... uri) throws TrackLoadException {
        return List.of();
    }

    @Override
    public TrackMixer getMixer() {
        return null;
    }

    @Override
    public TrackPlayer getTrackPlayer(Track track) {
        return null;
    }

    @Override
    public void addLoaderManager(TrackLoaderManager loader) {

    }

    @Override
    public void removeLoaderManager(TrackLoaderManager loader) {

    }

    @Override
    public List<TrackLoaderManager> getAvailableLoaders() {
        return List.of();
    }

    @Override
    public List<InputAudioDevice> getAvailableInputAudioDevices() {
        return List.of();
    }

    @Override
    public List<OutputAudioDevice> getAvailableOutputAudioDevices() {
        return List.of();
    }

    /**
     * Finds suitable loader for given format.
     *
     * @param format givven format
     * @return loader or empty Optional if none can handle
     */
    @Override
    public Optional<TrackLoaderManager> findLoaderFor(TrackFormat format) {
        return Optional.empty();
    }

    @Override
    public void close() {

    }
}