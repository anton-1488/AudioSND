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

/**
 * Main entry point for AudioSND.
 * <p>
 * Provides audio loading, mixing, and playback capabilities.
 * Engine must be {@link #init(AudioEngineConfig)} before use and {@link #close()} after.
 * </p>
 *
 * @see Track
 * @see TrackPlayer
 * @see TrackMixer
 *
 * @author Anton
 * @version 1.0
 */
public interface AudioEngine extends AutoCloseable {
    /**
     * Initializes engine with custom configuration.
     * @param config engine configuration
     * @throws IllegalStateException if already initialized
     * @throws AudioEngineException if initialization fails
     */
    void init(AudioEngineConfig config) throws AudioEngineException;

    /**
     * @return true if engine is initialized and ready
     */
    boolean isInitialized();

    /**
     * @return current engine configuration
     * @throws IllegalStateException if not initialized
     */
    AudioEngineConfig getConfig();

    /**
     * Return the local event manager for listening event into engine.
     * @return local event manager
     */
    EventManager getEventManager();

    // Load track from different sources.
    Track loadTrack(String path) throws TrackLoadException;
    Track loadTrack(InputStream stream) throws TrackLoadException;
    Track loadTrack(URI uri) throws TrackLoadException;

    List<Track> batchLoadTrack(String... path) throws TrackLoadException;
    List<Track> batchLoadTrack(InputStream... stream) throws TrackLoadException;
    List<Track> batchLoadTrack(URI... uri) throws TrackLoadException;


    TrackMixer getMixer();
    TrackPlayer getTrackPlayer(Track track);

    void addLoaderManager(TrackLoaderManager loader);
    void removeLoaderManager(TrackLoaderManager loader);
    List<TrackLoaderManager> getAvailableLoaders();

    // Getters for available audio device in yout PC.
    List<InputAudioDevice> getAvailableInputAudioDevices();
    List<OutputAudioDevice> getAvailableOutputAudioDevices();

    /**
     * Finds suitable loader for given format.
     * @return loader or empty Optional if none can handle
     */
    Optional<TrackLoaderManager> findLoaderFor(TrackFormat format);

    @Override
    void close();
}