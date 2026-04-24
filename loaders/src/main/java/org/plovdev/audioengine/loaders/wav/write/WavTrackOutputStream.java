package org.plovdev.audioengine.loaders.wav.write;

import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.format.TrackFormat;
import org.plovdev.audioengine.loaders.io.TrackOutputStream;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.plovdev.audioengine.loaders.ExportUtils.intToLittleEndian;
import static org.plovdev.audioengine.loaders.wav.struct.WavChunkId.DATA;
import static org.plovdev.audioengine.loaders.wav.write.WavTrackExporter.RIFF;
import static org.plovdev.audioengine.loaders.wav.write.WavTrackExporter.WAVE;

public class WavTrackOutputStream extends TrackOutputStream {
    public WavTrackOutputStream(Track track, OutputStream toWrite) {
        super(track, toWrite);
    }

    @Override
    protected void writeHead() throws IOException {
        TrackFormat format = track.getFormat();
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        WavChunkWriterManager chunkWriter = new WavChunkWriterManager(headerBuffer);
        chunkWriter.writeChunk(WavChunkId.FMT, format);

        int fmtSize = headerBuffer.size();
        int listSize = 0;

        if (track.getMetaData() != null && !track.getMetaData().isEmpty()) {
            chunkWriter.writeChunk(WavChunkId.ID3_HEAD, track);
            listSize = headerBuffer.size() - fmtSize;
        }

        int dataSize = headerBuffer.size() - fmtSize - listSize;
        int totalChunkSize = 4 + 8 + fmtSize + 8 + dataSize + (listSize > 0 ? (8 + listSize) : 0);

        toWrite.write(RIFF.getBytes());
        toWrite.write(intToLittleEndian(totalChunkSize));
        toWrite.write(WAVE.getBytes());
        headerBuffer.writeTo(toWrite);

        int dataChunkSize = track.getTrackData().remaining();
        toWrite.write(DATA.getChunk().getBytes());
        toWrite.write(intToLittleEndian(dataChunkSize));
    }

    @Override
    protected void writeEnd() throws IOException {
        if (track.getTrackData().remaining() % 2 != 0) {
            toWrite.write(0);
        }
    }
}