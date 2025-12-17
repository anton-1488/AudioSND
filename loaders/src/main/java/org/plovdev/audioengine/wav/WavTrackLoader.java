package org.plovdev.audioengine.wav;

import org.plovdev.audioengine.exceptions.TrackLoadException;
import org.plovdev.audioengine.loaders.LoadListener;
import org.plovdev.audioengine.loaders.TrackLoader;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.List;

public class WavTrackLoader implements TrackLoader {

    @Override
    public Track loadTrack(String path) throws TrackLoadException {
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new TrackLoadException("File not found: " + path);
            }

            byte[] fileData = readFileToByteArray(file);
            return parseWavData(fileData, file.getName());

        } catch (Exception e) {
            throw new TrackLoadException("Failed to load WAV file: " + path + " - " + e);
        }
    }

    @Override
    public Track loadTrack(InputStream stream) throws TrackLoadException {
        try {
            byte[] streamData = readStreamToByteArray(stream);
            return parseWavData(streamData, "stream.wav");

        } catch (Exception e) {
            throw new TrackLoadException("Failed to load WAV from stream: " + e);
        }
    }

    @Override
    public Track loadTrack(URI uri) throws TrackLoadException {
        if ("file".equals(uri.getScheme())) {
            return loadTrack(uri.getPath());
        }
        throw new TrackLoadException("Unsupported URI scheme: " + uri.getScheme());
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
        String lower = filename.toLowerCase();
        return lower.endsWith(".wav") || lower.endsWith(".wave");
    }

    @Override
    public boolean isSupported(InputStream stream) {
        return false;
    }

    @Override
    public boolean isSupported(URI uri) {
        return false;
    }

    @Override
    public boolean isSupporteds(String... filename) {
        return false;
    }

    @Override
    public boolean isSupporteds(InputStream... stream) {
        return false;
    }

    @Override
    public boolean isSupporteds(URI... uri) {
        return false;
    }

    @Override
    public void setLoadListener(LoadListener listener) {

    }

    @Override
    public LoadListener getLoadListener() {
        return null;
    }

    private Track parseWavData(byte[] wavData, String sourceName) {
        WavParser parser = new WavParser(wavData);
        parser.parseHeader();

        // Получаем аудиоданные
        ByteBuffer audioData = parser.getAudioDataAsBuffer();
        byte[] audioBytes = audioData.array();
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(audioBytes.length);
        directBuffer.put(audioBytes);
        directBuffer.flip();

        // Рассчитываем длительность
        long totalSamples = parser.getNumSamples();
        Duration duration = calculateDuration(totalSamples, parser.getSampleRate());

        // Создаем формат
        TrackFormat format = new TrackFormat(
                "wav",
                parser.getChannels(),
                parser.getBitDepth(),
                parser.getSampleRate(),
                true,
                ByteOrder.LITTLE_ENDIAN
        );

        // Создаем метаданные
        TrackMetadata metadata = new TrackMetadata();

        return new Track(directBuffer, duration, format, metadata);
    }

    private byte[] readFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }

    private byte[] readStreamToByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        return bos.toByteArray();
    }

    private Duration calculateDuration(long totalSamples, int sampleRate) {
        long seconds = totalSamples / sampleRate;
        long nanos = (totalSamples % sampleRate) * 1_000_000_000L / sampleRate;
        return Duration.ofSeconds(seconds).plusNanos(nanos);
    }
}