package org.plovdev.audioengine;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.callback.rx.EventManager;
import org.plovdev.audioengine.devices.InputAudioDevice;
import org.plovdev.audioengine.devices.OutputAudioDevice;
import org.plovdev.audioengine.exceptions.AudioEngineException;
import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.loaders.TrackLoader;
import org.plovdev.audioengine.loaders.TrackLoaderManager;
import org.plovdev.audioengine.mixer.NativeTrackMixer;
import org.plovdev.audioengine.mixer.TrackMixer;
import org.plovdev.audioengine.tracks.NativeTrackPlayer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.utils.AudioEngineConfig;
import org.plovdev.audioengine.utils.TrackLoaderSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class NativeAudioEngine implements AudioEngine {
    private static final Logger log = LoggerFactory.getLogger(NativeAudioEngine.class);
    private final List<TrackLoaderManager> loaderManagers = new CopyOnWriteArrayList<>();
    private final EventManager eventManager = new EventManager();
    private AudioEngineConfig config = AudioEngineConfig.load();
    private volatile boolean isInited = false;

    public NativeAudioEngine() {}

    public NativeAudioEngine(AudioEngineConfig config) {
        this.config = config;
        try {
            init(config);
        } catch (AudioEngineException e) {

            log.error("Initializing error: ", e);
        }
    }

    /**
     * Initializes engine with custom configuration.
     *
     * @param config engine configuration
     * @throws IllegalStateException if already initialized
     * @throws AudioEngineException  if initialization fails
     */
    @Override
    public synchronized void init(@NotNull AudioEngineConfig config) throws AudioEngineException {
        if (!isInited) {
            this.config = config;
            System.loadLibrary(config.getNativeLib().toString());
            TrackLoaderSearcher.getSearchedLoaders().forEach(this::addLoaderManager);
            _init();
            isInited = true;
        } else throw new AudioEngineException("Engine is already inited!");
    }

    /**
     * @return true if engine is initialized and ready
     */
    @Override
    public boolean isInitialized() {
        return isInited;
    }

    /**
     * @return current engine configuration
     * @throws IllegalStateException if not initialized
     */
    @Override
    public AudioEngineConfig getConfig() {
        return config;
    }

    /**
     * Return the local event manager for listening event into engine.
     *
     * @return local event manager
     */
    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public Track loadTrack(@NotNull String path) throws TrackLoadException {
        checkIfInited();
        for (TrackLoaderManager manager : getAvailableLoaders()) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupported(path)) {
                return loader.loadTrack(path);
            }
        }
        throw new TrackLoadException("Loader not found fot this track source.");
    }

    @Override
    public Track loadTrack(@NotNull InputStream stream) throws TrackLoadException {
        checkIfInited();
        for (TrackLoaderManager manager : getAvailableLoaders()) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupported(stream)) {
                return loader.loadTrack(stream);
            }
        }
        throw new TrackLoadException("Loader not found fot this track source.");
    }

    @Override
    public Track loadTrack(@NotNull URI uri) throws TrackLoadException {
        checkIfInited();
        for (TrackLoaderManager manager : getAvailableLoaders()) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupported(uri)) {
                return loader.loadTrack(uri);
            }
        }
        throw new TrackLoadException("Loader not found fot this track source.");
    }

    @Override
    public List<Track> batchLoadTrack(String... path) throws TrackLoadException {
        checkIfInited();
        for (TrackLoaderManager manager : getAvailableLoaders()) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupporteds(path)) {
                return loader.batchLoadTrack(path);
            }
        }
        return List.of();
    }

    @Override
    public List<Track> batchLoadTrack(InputStream... stream) throws TrackLoadException {
        checkIfInited();
        for (TrackLoaderManager manager : getAvailableLoaders()) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupporteds(stream)) {
                return loader.batchLoadTrack(stream);
            }
        }
        return List.of();
    }

    @Override
    public List<Track> batchLoadTrack(URI... uri) throws TrackLoadException {
        checkIfInited();
        for (TrackLoaderManager manager : getAvailableLoaders()) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupporteds(uri)) {
                return loader.batchLoadTrack(uri);
            }
        }
        return List.of();
    }

    @Override
    public TrackMixer getMixer() {
        checkIfInited();
        return new NativeTrackMixer();
    }

    @Override
    public TrackPlayer getTrackPlayer(@NotNull Track track) {
        checkIfInited();
        return new NativeTrackPlayer(track, getAvailableOutputAudioDevices().getFirst());
    }

    @Override
    public void addLoaderManager(@NotNull TrackLoaderManager loader) {
        loaderManagers.add(loader);
    }

    @Override
    public void removeLoaderManager(@NotNull TrackLoaderManager loader) {
        loaderManagers.remove(loader);
    }

    @Override
    public List<TrackLoaderManager> getAvailableLoaders() {
        return loaderManagers;
    }

    @Override
    public List<InputAudioDevice> getAvailableInputAudioDevices() {
        checkIfInited();
        return List.of();
    }

    @Override
    public List<OutputAudioDevice> getAvailableOutputAudioDevices() {
        checkIfInited();
        return List.of();
    }

    /**
     * Finds suitable loader for given format.
     *
     * @param format givven format
     * @return loader or empty Optional if none can handle
     */
    @Override
    public Optional<TrackLoaderManager> findLoaderFor(@NotNull TrackFormat format) {
        for (TrackLoaderManager manager : loaderManagers) {
            TrackLoader loader = manager.getTrackLoader();
            if (loader.isSupported(format.extension())) {
                return Optional.of(manager);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized void close() {
        if (!isInited) {
            return;
        }
        loaderManagers.clear();
        isInited = false;
        _cleanup();
    }

    private void checkIfInited() {
        if (!isInited) {
            throw new AudioEngineException("AudioSND hasn't been inited!");
        }
    }


    //====== Natives ======\\
    private native void _init();

    private native void _cleanup();
}