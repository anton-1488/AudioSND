package org.plovdev.audioengine.api;

import org.jetbrains.annotations.NotNull;

/**
 * Controls audio recording from input device.
 * <p>
 * Recorder follows similar lifecycle to player:
 * <ol>
 *   <li>{@link #start()} - begin recording</li>
 *   <li>{@link #pause()} - temporarily pause</li>
 *   <li>{@link #stop()} - stop and get recorded track</li>
 *   <li>{@link #close()} - release resources</li>
 * </ol>
 * </p>
 *
 * @author Anton
 * @version 1.0
 */
public interface AudioRecorder extends AudioApi {
    /**
     * Starts recording from current input device.
     * Creates new track in memory for captured audio.
     *
     * @throws IllegalStateException if recorder is not initialized or already recording
     */
    void start();

    /**
     * Pauses recording at current position.
     * Recording can be resumed with {@link #start()}.
     *
     * @throws IllegalStateException if not recording
     */
    void pause();

    /**
     * Stops recording and returns captured audio as Track.
     * Track can be saved, played, or processed further.
     *
     * @return recorded audio track (never null)
     * @throws IllegalStateException if no recording in progress
     */
    @NotNull
    Track stop();

    /**
     * Returns currently recorded track while recording is in progress.
     * Useful for real-time monitoring or partial saves.
     *
     * @return track with recorded audio so far
     * @throws IllegalStateException if not recording
     */
    @NotNull
    Track getCurrentTrack();

    /**
     * Sets input gain (amplification) for recording.
     *
     * @param gain multiplier (1.0 = normal, >1.0 = boost, <1.0 = attenuate)
     * @throws IllegalArgumentException if gain <= 0
     */
    void setGain(float gain);
}