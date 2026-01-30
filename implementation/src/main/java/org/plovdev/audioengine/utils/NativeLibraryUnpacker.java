package org.plovdev.audioengine.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.plovdev.audioengine.utils.AudioEngineConfig.NativeLib;

public class NativeLibraryUnpacker {
    private static final Logger log = LoggerFactory.getLogger(NativeLibraryUnpacker.class);
    private NativeLib toUnpack;

    private static final String WIN_LIB_NAME = "audio-snd.dll";
    private static final String MAC_LIB_NAME = "audio-snd.dylib";
    private static final String UNIX_LIB_NAME = "audio-snd.so";

    public NativeLibraryUnpacker(NativeLib nativeLib) {
        toUnpack = nativeLib;
    }

    public NativeLib getToUnpack() {
        return toUnpack;
    }

    public void setToUnpack(NativeLib toUnpack) {
        this.toUnpack = toUnpack;
    }

    public String unpackLib() {
        String unpackPath = "/Users/mac/IdeaProjects/AudioSND/";

        String libName = getLibName();
        log.debug("Unpacking native lib: {}", libName);
        String fullName = unpackPath + libName;
        Path libPath = Path.of(fullName);

        if (!Files.exists(libPath)) {
            try (InputStream libStream = Objects.requireNonNull(getClass().getResourceAsStream("/nativies/libs/" + libName))) {
                Files.createFile(libPath);
                Files.write(libPath, libStream.readAllBytes());
                log.debug("Native lib unpacked to {}", fullName);
            } catch (Exception e) {
                throw new RuntimeException("Cann't unpack native library: ", e);
            }
        }
        return fullName;
    }

    private String getLibName() {
        String osName = System.getProperty("os.name").trim().toLowerCase();
        log.debug("OS name: {}", osName);
        if (osName.contains("win")) {
            return WIN_LIB_NAME;
        } else if (osName.contains("mac")) {
            return MAC_LIB_NAME;
        } else {
            return UNIX_LIB_NAME;
        }
    }
}