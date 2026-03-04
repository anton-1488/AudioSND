package org.plovdev.audioengine.engine.config;

import java.util.Objects;

public class EngineConfigBuilderImpl implements AudioEngineConfig.AudioEngineConfigBuilder {
    private AudioEngineConfig.NativeLib nativeLib = AudioEngineConfig.NativeLib.DEFAULT;
    private String banner;
    private boolean ignoreNativeWhenFail;

    @Override
    public AudioEngineConfig.AudioEngineConfigBuilder nativeLib(AudioEngineConfig.NativeLib lib) {
        Objects.requireNonNull(lib);
        nativeLib = lib;
        return this;
    }

    @Override
    public AudioEngineConfig.AudioEngineConfigBuilder banner(String banner) {
        Objects.requireNonNull(banner);
        this.banner = banner;
        return this;
    }

    @Override
    public AudioEngineConfig.AudioEngineConfigBuilder ignoreNativeWhenFail(boolean inwf) {
        ignoreNativeWhenFail = inwf;
        return this;
    }

    @Override
    public AudioEngineConfig configurate() {
        return new AudioEngineConfig(nativeLib, banner, ignoreNativeWhenFail);
    }
}
