package org.plovdev.audioengine.wav;

import org.plovdev.audioengine.exceptions.TrackExportException;
import org.plovdev.audioengine.loaders.TrackExporter;
import org.plovdev.audioengine.tracks.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class WavTrackExporter implements TrackExporter {
    private final Logger log = LoggerFactory.getLogger(WavTrackExporter.class);

    @Override
    public void save(Track track, OutputStream outputStream) {
        try {

        } catch (Exception e) {
            throw new TrackExportException(e.getMessage());
        }
    }
}