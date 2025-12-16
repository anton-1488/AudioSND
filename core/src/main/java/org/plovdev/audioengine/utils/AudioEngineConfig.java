package org.plovdev.audioengine.utils;

import java.util.prefs.Preferences;

/**
 * Configuration manager for the audio engine settings.
 * <p>
 * Handles persistent storage and retrieval of core audio parameters
 * using Java {@link Preferences} API.
 * </p>
 *
 * @author Anton
 * @since 1.0
 */
public class AudioEngineConfig {
    // Preferences engine keys
    private static final String NATIVE_LIB_KEY = "native-lib";
    private static final String BUFFER_SIZE_KEY = "buffer-size";
    private static final String PLAYER_THREADS_KEY = "plyer-threads-size";

    // Configurable fields
    private NativeLib nativeLib;
    private int bufferSize;
    private int bufferCount;

    public AudioEngineConfig() {}

    /**
     * Creates a configuration instance with specified parameters.
     *
     * @param nativeLib native audio library implementation
     * @param bufferSize audio buffer size in kb
     * @param bufferCount number of threads in player pool
     */
    public AudioEngineConfig(NativeLib nativeLib, int bufferSize, int bufferCount) {
        this.nativeLib = nativeLib;
        this.bufferSize = bufferSize;
        this.bufferCount = bufferCount;
    }

    /**
     * Loads configuration using default preferences key "AudioSND".
     * <p>
     * If no saved configuration exists, returns default values:
     * {@code NativeLib.DEFAULT, bufferSize=8, playerThreadsSize=25}.
     * </p>
     *
     * @return loaded or default configuration
     */
    public static AudioEngineConfig load() {
        return load("AudioSND");
    }

    /**
     * Loads configuration from the specified preferences key.
     *
     * @param prefsKey preferences node key
     * @return loaded configuration
     * @see Preferences#userRoot()
     */
    public static AudioEngineConfig load(String prefsKey) {
        Preferences prefs = Preferences.userRoot().node(prefsKey);
        NativeLib lib = NativeLib.valueOf(prefs.get(NATIVE_LIB_KEY, NativeLib.DEFAULT.name()));
        int bufferSize = prefs.getInt(BUFFER_SIZE_KEY, 4096);
        int count = prefs.getInt(PLAYER_THREADS_KEY, 20);

        return new AudioEngineConfig(lib, bufferSize, count);
    }

    /**
     * Saves current configuration using default key "AudioSND".
     * <p>
     * Configuration is persisted to OS-specific user preferences storage.
     * </p>
     */
    public void save() {
        save("AudioSND");
    }

    /**
     * Saves current configuration to the specified preferences key.
     *
     * @param prefsKey target preferences node key
     */
    public void save(String prefsKey) {
        Preferences prefs = Preferences.userRoot().node(prefsKey);
        prefs.put(NATIVE_LIB_KEY, nativeLib.name());
        prefs.putInt(BUFFER_SIZE_KEY, bufferSize);
        prefs.putInt(PLAYER_THREADS_KEY, bufferCount);
    }

    // Getters and setters

    public NativeLib getNativeLib() {
        return nativeLib;
    }

    public void setNativeLib(NativeLib nativeLib) {
        this.nativeLib = nativeLib;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets audio buffer size.
     *
     * @param bufferSize new buffer size in kb
     * @throws IllegalArgumentException if bufferSize < 0
     */

    public void setBufferSize(int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }
        this.bufferSize = bufferSize;
    }

    public int getbufferCount() {
        return bufferCount;
    }

    public void setbufferCount(int bufferCount) {
        if (bufferCount < 1) {
            throw new IllegalArgumentException("Buffers count must be large than 0");
        }
        this.bufferCount = bufferCount;
    }

    /**
     * Available native audio library implementations.
     */
    public enum NativeLib {
        DEFAULT("audio-snd");
        private final String libName;

        NativeLib(String name) {
            libName = name;
        }

        @Override
        public String toString() {
            return libName;
        }
    }
}