package org.plovdev.audioengine.engineconfig;

import java.util.Objects;
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
    private static final String IGNORE_NATIVE_WHEN_FAIL = "inwf";
    private static final String BANNER = "banner";
    public static final String VERSION = "1.0.0-BETA";
    private static final String DEFAULT_PREFS_KEY = "AudioSND";

    // Configurable fields
    private NativeLib nativeLib;
    private String banner;
    private boolean ignoreNativeWhenFail;

    /**
     * Creates a configuration instance with specified parameters.
     *
     */
    public AudioEngineConfig(NativeLib lib, String banner, boolean inwf) {
        setNativeLib(lib);
        setBanner(banner);
        setIgnoreNativeWhenFail(inwf);
    }

    /**
     * Loads configuration using default preferences key "AudioSND".
     *
     * @return loaded or default configuration
     */
    public static AudioEngineConfig load() {
        return load(DEFAULT_PREFS_KEY);
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
        String banner = prefs.get(BANNER, String.format("""
                                      █████╗ ██╗   ██╗██████╗ ██╗ ██████╗    ███████╗███╗   ██╗██████╗
                                     ██╔══██╗██║   ██║██╔══██╗██║██╔═══██╗   ██╔════╝████╗  ██║██╔══██╗
                                     ███████║██║   ██║██║  ██║██║██║   ██║   ███████╗██╔██╗ ██║██║  ██║
                                     ██╔══██║██║   ██║██║  ██║██║██║   ██║   ╚════██║██║╚██╗██║██║  ██║
                                     ██║  ██║╚██████╔╝██████╔╝██║╚██████╔╝   ███████║██║ ╚████║██████╔╝
                                     ╚═╝  ╚═╝ ╚═════╝ ╚═════╝ ╚═╝ ╚═════╝    ╚══════╝╚═╝  ╚═══╝╚═════╝
                                                High-Performance Audio Engine v%s
                """, VERSION));
        boolean inwf = prefs.getBoolean(IGNORE_NATIVE_WHEN_FAIL, false);

        return new AudioEngineConfig(lib, banner, inwf);
    }

    /**
     * Saves current configuration using default key "AudioSND".
     * <p>
     * Configuration is persisted to OS-specific user preferences storage.
     * </p>
     */
    public void save() {
        save(DEFAULT_PREFS_KEY);
    }

    /**
     * Saves current configuration to the specified preferences key.
     *
     * @param prefsKey target preferences node key
     */
    public void save(String prefsKey) {
        Preferences prefs = Preferences.userRoot().node(prefsKey);
        prefs.put(NATIVE_LIB_KEY, nativeLib.name());
        prefs.put(BANNER, banner);
        prefs.putBoolean(IGNORE_NATIVE_WHEN_FAIL, ignoreNativeWhenFail);
    }

    // Getters and setters

    public NativeLib getNativeLib() {
        return nativeLib;
    }

    public void setNativeLib(NativeLib nativeLib) {
        this.nativeLib = Objects.requireNonNull(nativeLib);
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = Objects.requireNonNull(banner);
    }

    public boolean isIgnoreNativeWhenFail() {
        return ignoreNativeWhenFail;
    }

    public void setIgnoreNativeWhenFail(boolean ignoreNativeWhenFail) {
        this.ignoreNativeWhenFail = ignoreNativeWhenFail;
    }

    public static AudioEngineConfigBuilder builder() {
        return new EngineConfigBuilderImpl();
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

    /**
     * AudioEngine config builder.
     * @see EngineConfigBuilderImpl
     *
     * @author Anton
     * @since 1.0
     */
    public interface AudioEngineConfigBuilder {
        AudioEngineConfigBuilder nativeLib(NativeLib lib);
        AudioEngineConfigBuilder banner(String banner);
        AudioEngineConfigBuilder ignoreNativeWhenFail(boolean inwf);

        AudioEngineConfig configurate();
    }
}