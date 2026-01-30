package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.loaders.LoadListener;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.TrackLoader;
import org.plovdev.audioengine.loaders.wav.chunks.DataChunk;
import org.plovdev.audioengine.loaders.wav.chunks.FormatChunk;
import org.plovdev.audioengine.loaders.wav.chunks.ListChunk;
import org.plovdev.audioengine.loaders.wav.chunks.TagEntry;
import org.plovdev.audioengine.loaders.wav.read.WavParser;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.plovdev.audioengine.loaders.ExportUtils.getFile;

public class WavTrackLoader implements TrackLoader {
    private static final Logger log = LoggerFactory.getLogger(WavTrackLoader.class);
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

            ListChunk listChunk = parser.getListChunk();

            Duration duration = Duration.ofMillis(TrackFormatUtils.calculateDurationMs(format, chunk.getSize()));

            TrackMetadata metadata = new TrackMetadata();
            metadata.setDuration(duration);
            metadata.setChannels(format.channels());
            metadata.setBitrate(format.bitRate());
            metadata.setBitDepth(format.bitsPerSample());
            metadata.setSampleRate(format.sampleRate());

            if (listChunk != null) {
                for (Chunk tag : listChunk.getEntries()) {
                    TagEntry entry = (TagEntry) tag;
                    switch (tag.getChunk()) {
                        case INAME -> metadata.setTitle(entry.getContent());
                        case ISFT -> metadata.setPublisher(entry.getContent());
                        case ICRD -> metadata.setCreationDate(parsePartialDate(entry.getContent()).getTime());
                    }
                }
            }

            return new Track(chunk.getData(), duration, format, metadata);
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
        // Копируем первые 12 байт для проверки
        byte[] header = new byte[12];

        try {
            if (stream.markSupported()) {
                stream.mark(12);
            }

            int totalRead = 0;
            while (totalRead < 12) {
                int read = stream.read(header, totalRead, 12 - totalRead);
                if (read == -1) {
                    return false;
                }
                totalRead += read;
            }

            String riff = new String(header, 0, 4, StandardCharsets.US_ASCII);
            if (!riff.equals("RIFF")) {
                log.info("File is not RIFF based! {}", riff);
                return false;
            }

            String wave = new String(header, 8, 4, StandardCharsets.US_ASCII);
            if (!wave.equals("WAVE")) {
                log.info("Not WAVE file type! {}", wave);
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            // Возвращаем позицию
            try {
                if (stream.markSupported()) {
                    stream.reset();
                }
            } catch (IOException e) {
                log.warn("Не удалось сбросить поток", e);
            }
        }
    }

    @Override
    public boolean isSupported(URI uri) {
        return isSupported(uri.getPath());
    }

    private Calendar parsePartialDate(String input) {
        String[] parts = input.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = (parts.length > 1) ? Integer.parseInt(parts[1]) - 1 : 0; // Месяц 0–11
        int day = (parts.length > 2) ? Integer.parseInt(parts[2]) : 1;


        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
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