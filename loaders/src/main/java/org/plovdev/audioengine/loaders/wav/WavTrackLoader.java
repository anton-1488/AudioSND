package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.loaders.LoadListener;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.TrackLoader;
import org.plovdev.audioengine.loaders.wav.chunks.DataChunk;
import org.plovdev.audioengine.loaders.wav.chunks.FormatChunk;
import org.plovdev.audioengine.loaders.wav.read.WavParser;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.plovdev.audioengine.loaders.ExportUtils.getFile;

public class WavTrackLoader implements TrackLoader {
    private final List<PathLocator> locators = new CopyOnWriteArrayList<>();
    private LoadListener loadListener = null;

    public void addLoactor(PathLocator locator) {
        locators.add(locator);
    }

    @Override
    public Track loadTrack(String path) throws TrackLoadException {
        File file = getFile(path, locators);
        try (InputStream stream = new FileInputStream(file)) {
            return loadTrack(stream);
        } catch (Exception e) {
            throw new TrackLoadException("Failed to load WAV file: " + path + " - " + e);
        }
    }

    @Override
    public Track loadTrack(InputStream stream) throws TrackLoadException {
        try {
            WavParser parser = new WavParser(stream);
            parser.parse();

            DataChunk chunk = parser.getDataChunk();
            FormatChunk formatChunk = parser.getFormatChunk();
            TrackFormat format = formatChunk.getFormat();

            return new Track(chunk.getData(), Duration.ofMillis(TrackFormatUtils.calculateDurationMs(format, chunk.getSize())), format, new TrackMetadata());
        } catch (Exception e) {
            throw new TrackLoadException("Failed to load WAV from stream: " + e);
        }
    }

    @Override
    public Track loadTrack(URI uri) throws TrackLoadException {
        return switch (uri.getScheme()) {
            case "file" -> loadTrack(uri.getPath());
            case "https", "http" -> {
                try (InputStream stream = uri.toURL().openStream()) {
                    yield loadTrack(stream);
                } catch (Exception e) {
                    throw new TrackLoadException(e.getMessage());
                }
            }
            default -> throw new TrackLoadException("Unsupported URI scheme: " + uri.getScheme());
        };
    }

    @Override
    public TrackMetadata readTrackMetadata(String src) {
        return null;
    }

    @Override
    public TrackMetadata readTrackMetadata(InputStream src) {
        return null;
    }

    @Override
    public TrackMetadata readTrackMetadata(URI src) {
        return null;
    }

    @Override
    public TrackFormat getTrackFormat(String src) {
        return null;
    }

    @Override
    public TrackFormat getTrackFormat(InputStream src) {
        return null;
    }

    @Override
    public TrackFormat getTrackFormat(URI src) {
        return null;
    }

    @Override
    public boolean isSupported(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase().trim();
        lower = lower.startsWith(".") ? lower : "." + lower;
        return lower.endsWith(".wav") || lower.endsWith(".wave");
    }

    @Override
    public boolean isSupported(InputStream stream) {
        return false;
    }

    @Override
    public boolean isSupported(URI uri) {
        return isSupported(uri.getPath());
    }

    @Override
    public void setLoadListener(LoadListener listener) {
        loadListener = listener;
    }

    @Override
    public LoadListener getLoadListener() {
        return loadListener;
    }
}