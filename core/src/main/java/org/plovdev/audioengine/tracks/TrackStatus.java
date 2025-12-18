package org.plovdev.audioengine.tracks;

/**
 * Statuses of track playing lifecycle.
 * @see TrackPlayer
 * @see Track
 *
 * @author Anton
 * @version 1.0
 */
public enum TrackStatus {
    INITED, PLAYING, PAUSED, STOPPED, DESTROYED, UNAVAILABLE
}