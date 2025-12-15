package org.plovdev.audioengine.mixer;

import org.plovdev.audioengine.exceptions.MixingException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.List;

public class NativeTrackMixer implements TrackMixer {
    /**
     * Setup output track format(after mixing)
     *
     * @param format
     */
    @Override
    public void setOutputFormat(TrackFormat format) {

    }

    /**
     * Get setuped output track format
     */
    @Override
    public TrackFormat getOutputFormat() {
        return null;
    }

    @Override
    public void addTrack(Track track) {

    }

    @Override
    public void removeTrack(Track track) {

    }

    /**
     * Get all tracks, which will be mixing.
     *
     * @return mixing tracks
     */
    @Override
    public List<Track> getMixingTracks() {
        return List.of();
    }

    /**
     * Clear tracks from mixer
     */
    @Override
    public void clearTracks() {

    }

    /**
     * Mix all track in list.
     *
     * @throws MixingException when mixing failed.
     */
    @Override
    public Track doMixing() throws MixingException {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns tracks in mixer.
     *
     * @return mixing tracks.
     */
    @Override
    public int getTrackCount() {
        return 0;
    }
}