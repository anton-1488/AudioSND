package org.plovdev.audioengine.player;

import org.plovdev.audioengine.tracks.Track;

/**
 * Statuses of track playing lifecycle.
 * @see TrackPlayer
 * @see Track
 *
 * @author Anton
 * @version 1.0
 */
public enum TrackPlayerStatus {
    INITED, PLAYING, PAUSED, STOPPED, DESTROYED, UNAVAILABLE
}