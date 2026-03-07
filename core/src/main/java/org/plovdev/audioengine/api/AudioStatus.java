package org.plovdev.audioengine.api;

/**
 * Statuses of audio playback/recording lifecycle.
 * <p>
 * Used by both {@link TrackPlayer} and {@link AudioRecorder} to indicate
 * current state of audio operations.
 * </p>
 *
 * @author Anton
 * @version 1.0
 */
public enum AudioStatus {
    /**
     * Initialized, ready to start
     */
    INITED,

    /**
     * Currently playing/recording
     */
    RUNNING,

    /**
     * Temporarily paused
     */
    PAUSED,

    /**
     * Stopped, can be restarted
     */
    STOPPED,

    /**
     * Released, cannot be used anymore
     */
    DESTROYED,

    /**
     * Device not available
     */
    UNAVAILABLE
}