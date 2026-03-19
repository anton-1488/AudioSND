package org.plovdev.audioengine.loaders.flac;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.format.TrackFormat;
import org.plovdev.audioengine.loaders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.plovdev.audioengine.format.TrackFormat.AudioCodec.FLAC;

public class FlacTrackLoaderManager implements TrackLoaderManager {
    private static final List<TrackFormat.AudioCodec> supportedCodecs = List.of(FLAC);
    private static final Logger log = LoggerFactory.getLogger(FlacTrackLoaderManager.class);

    private final FlacTrackLoader flacLoader;
    private final FlacTrackExporter flacExporter;
    private final FlacTrackEncoder flacEncoder;
    private final FlacTrackDecoder flacDecoder;

    public FlacTrackLoaderManager() {
        this.flacLoader = new FlacTrackLoader();
        this.flacExporter = new FlacTrackExporter();
        this.flacEncoder = new FlacTrackEncoder();
        this.flacDecoder = new FlacTrackDecoder();
    }

    @Override
    public @NotNull TrackLoader getTrackLoader() {
        return flacLoader;
    }

    @Override
    public @NotNull TrackExporter getTrackExporter() {
        return flacExporter;
    }

    @Override
    public @NotNull TrackEncoder getTrackEncoder() {
        return flacEncoder;
    }

    @Override
    public @NotNull TrackDecoder getTrackDecoder() {
        return flacDecoder;
    }

    /**
     * Checks if this loader supports the given file.
     *
     * @param file file to check
     * @return true if file format is supported
     */
    @Override
    public boolean isSupported(@NotNull File file) {
        try {
            return isSupported(new FileInputStream(file));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if this loader supports the given audio format.
     * <p>
     * Useful for quick pre-validation without accessing actual file/stream.
     * </p>
     *
     * @param format audio format to check
     * @return true if format is supported
     */
    @Override
    public boolean isSupported(@NotNull TrackFormat format) {
        return supportedCodecs.contains(format.audioCodec());
    }

    /**
     * Checks if this loader supports the given input stream.
     *
     * @param stream stream to check
     * @return true if stream format is supported
     */
    @Override
    public boolean isSupported(InputStream stream) {
        byte[] header = new byte[4];
        try {
            if (stream.markSupported()) {
                stream.mark(4);
            }
            int totalRead = stream.read(header);
            String wave = new String(header, StandardCharsets.US_ASCII);
            if (!wave.equals("fLaC")) {
                log.info("Not FLAC file! {}", wave);
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (stream.markSupported()) {
                    stream.reset();
                }
            } catch (IOException e) {
                log.warn("Не удалось сбросить поток", e);
            }
        }
    }

    /**
     * Checks if this loader supports the given URI.
     *
     * @param uri URI to check
     * @return true if URI protocol and format are supported
     */
    @Override
    public boolean isSupported(@NotNull URI uri) {
        try (InputStream stream = uri.toURL().openStream()) {
            return isSupported(stream);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void registerPathLocator(@NotNull PathLocator locator) {
        flacLoader.addLoactor(locator);
    }
}