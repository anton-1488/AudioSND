package org.plovdev.audioengine.mixer;

import org.plovdev.audioengine.exceptions.MixingException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NativeTrackMixer implements TrackMixer {
    private TrackFormat outputFormat = WavTrackFormatFactory.wav16bitStereo44kHz();
    private final List<Track> mixingTracks = new CopyOnWriteArrayList<>();
    /**
     * Setup output track format(after mixing)
     *
     * @param format output format
     */
    @Override
    public void setOutputFormat(TrackFormat format) {
        outputFormat = format;
    }

    /**
     * Get setuped output track format
     */
    @Override
    public TrackFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public void addTrack(Track track) {
        mixingTracks.add(track);
    }

    @Override
    public void removeTrack(Track track) {
        mixingTracks.remove(track);
    }

    /**
     * Get all tracks, which will be mixing.
     *
     * @return mixing tracks
     */
    @Override
    public List<Track> getMixingTracks() {
        return mixingTracks;
    }

    /**
     * Clear tracks from mixer
     */
    @Override
    public void clearTracks() {
        mixingTracks.clear();
    }

    /**
     * Mix all track in list.
     *
     * @throws MixingException when mixing failed.
     */
    @Override
    public Track doMixing() throws MixingException {
        if (isEmpty()) {
            throw new MixingException("No tracks for mixing.");
        }
        return _doMixing(mixingTracks, outputFormat);
    }

    @Override
    public boolean isEmpty() {
        return mixingTracks.isEmpty();
    }

    /**
     * Returns tracks in mixer.
     *
     * @return mixing tracks.
     */
    @Override
    public int getTrackCount() {
        return mixingTracks.size();
    }

    private native Track _doMixing(List<Track> tracks, TrackFormat format);
}