package org.plovdev.audioengine.tracks;

import java.time.Duration;

/**
 * Controls playback of a single audio track.
 * <p>
 * Player follows strict lifecycle:
 * <ol>
 *   <li>{@link #play()} - start playback</li>
 *   <li>{@link #pause()} - control playback</li>
 *   <li>{@link #stop()} - stop playback</li>
 *   <li>{@link #close()} - release resources</li>
 * </ol>
 * </p>
 *
 * @author Anton
 * @version 1.0
 * @see Track
 */
public interface TrackPlayer extends AutoCloseable {
    void initPlayer();

    /**
     * Starts or resumes playback.
     *
     * @throws IllegalStateException if player is not prepared
     */
    void play();

    /**
     * Pauses playback. Playback can be resumed with {@link #play()}.
     *
     * @throws IllegalStateException if player is not playing
     */
    void pause();

    /**
     * Stops playback and resets position to beginning.
     * Player remains prepared and can be played again.
     *
     * @throws IllegalStateException if player is not active
     */
    void stop();


    /**
     * Gets current playback volume.
     */
    float getVolume();

    /**
     * Gets current playback speed.
     */
    float getSpeed();

    /**
     * Gets total cycles count.
     */
    int getCycles();

    /**
     * Gets current playing cycle.
     */
    int getCurrentCycle();

    /**
     * @return Returns current player status.
     */
    TrackStatus getStatus();

    /**
     * Gets current playback time.
     */
    Duration getCurrentTime();


    /**
     * Sets playback volume.
     *
     * @param volume volume (0.0 = silent, 1.0 = max)
     * @throws IllegalArgumentException if volume out of range
     */
    void setVolume(float volume);

    /**
     * Sets playback speed multiplier.
     *
     * @param speed speed (0.5 = half, 1.0 = normal, 2.0 = double)
     * @throws IllegalArgumentException if speed out of range
     * @throws UnsupportedOperationException if speed change not supported
     */
    void setSpeed(float speed);

    /**
     * Sets loop count.
     *
     * @param count number of times to repeat (0 = no loop, -1 = infinite)
     * @throws IllegalArgumentException if count < -1
     */
    void setLoopCount(int count);

    /**
     * Seeks to specific position in track.
     *
     * @param position position to seek to
     *
     * @throws IllegalArgumentException if position is out of bounds
     * @throws IllegalStateException if player is not prepared
     */
    void seek(Duration position);

    /**
     * Closes player and releases all resources.
     * Player cannot be used after close.
     */
    @Override
    void close();
}