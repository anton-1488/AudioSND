package org.plovdev.audioengine.mixer;

import org.plovdev.audioengine.exceptions.MixingException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.List;

/**
 * Tracks mixing manager.
 *
 * @author Anton
 * @version 1.0
 */
public interface TrackMixer {
    /**
     * Setup output track format(after mixing)
     */
    void setOutputFormat(TrackFormat format);

    /**
     * Get setuped output track format
     */
    TrackFormat getOutputFormat();


    void addTrack(Track track);


    void removeTrack(Track track);

    /**
     * Get all tracks, which will be mixing.
     * @return mixing tracks
     */
    List<Track> getMixingTracks();

    /**
     * Clear tracks from mixer
     */
    void clearTracks();

    /**
     * Mix all track in list.
     * @throws MixingException when mixing failed.
     */
    Track doMixing() throws MixingException;


    boolean isEmpty();

    /**
     * Returns tracks in mixer.
     * @return mixing tracks.
     */
    int getTrackCount();
}