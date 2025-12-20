package org.plovdev.audioengine.loaders.wav.write;

import org.plovdev.audioengine.exceptions.TrackExportException;
import org.plovdev.audioengine.loaders.TrackExporter;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import static org.plovdev.audioengine.loaders.ExportUtils.intToLittleEndian;

public class WavTrackExporter implements TrackExporter {
    public static final String RIFF = "RIFF";
    public static final String WAVE = "WAVE";

    private final Logger log = LoggerFactory.getLogger(WavTrackExporter.class);

    @Override
    public void save(Track track, OutputStream outputStream) {
        try {
            TrackFormat format = track.getFormat();
            int channels = format.channels();
            int sampleRate = format.sampleRate();
            int bitDepth = format.bitsPerSample();
            ByteBuffer trackData = track.getTrackData().rewind();

            int blockAlign = channels * (bitDepth / 8);
            int byteRate = sampleRate * blockAlign;
            int dataChunkSize = trackData.remaining();

            outputStream.write(RIFF.getBytes());
            outputStream.write(intToLittleEndian(36 + dataChunkSize));
            outputStream.write(WAVE.getBytes());

            WavChunkWriter chunkWriter = new WavChunkWriter(outputStream);

            chunkWriter.writeFormat(format);
            chunkWriter.writeDataChunk(trackData, format);
        } catch (Exception e) {
            throw new TrackExportException(e.getMessage());
        }
    }
}