package org.plovdev.audioengine.player;

import org.plovdev.audioengine.devices.AudioDeviceInfo;
import org.plovdev.audioengine.tracks.Track;

import java.time.Duration;

/**
 * Controls playback of a single audio track.
 * <p>
 * Player follows strict lifecycle:
 * <ol>
 *   <li>{@link #initPlayer()} - prepare player (called automatically)</li>
 *   <li>{@link #play()} - start playback</li>
 *   <li>{@link #pause()} - pause playback</li>
 *   <li>{@link #stop()} - stop playback</li>
 *   <li>{@link #close()} - release all resources</li>
 * </ol>
 * </p>
 * <p>
 * Additional controls:
 * <ul>
 *   <li>{@link #setVolume(float)} - adjust volume in real-time</li>
 *   <li>{@link #setSpeed(float)} - change playback speed</li>
 *   <li>{@link #setLoopCount(int)} - configure looping</li>
 *   <li>{@link #seek(Duration)} - jump to position</li>
 *   <li>{@link #setAudioDevice(AudioDeviceInfo)} - switch output device on the fly</li>
 * </ul>
 * </p>
 *
 * @author Anton
 * @version 1.0
 * @see Track
 */
public interface TrackPlayer extends AutoCloseable {

    /**
     * Initializes the player with track format and opens audio device.
     * Called automatically in constructor, but can be called manually if needed.
     *
     * @throws IllegalStateException if already initialized
     */
    void initPlayer();

    /**
     * Starts or resumes playback from current position.
     * If player is stopped, starts from beginning.
     * If player is paused, resumes from paused position.
     *
     * @throws IllegalStateException if player is not initialized
     */
    void play();

    /**
     * Pauses playback at current position.
     * Playback can be resumed with {@link #play()}.
     *
     * @throws IllegalStateException if player is not playing
     */
    void pause();

    /**
     * Stops playback and resets position to beginning.
     * Player remains initialized and can be played again.
     *
     * @throws IllegalStateException if player is not active
     */
    void stop();

    /**
     * Gets current playback volume.
     *
     * @return volume value (typically 0.0 = silent, 1.0 = normal, >1.0 = boost)
     */
    float getVolume();

    /**
     * Gets current playback speed.
     *
     * @return speed multiplier (1.0 = normal)
     */
    float getSpeed();

    /**
     * Gets total cycles (loops) count.
     *
     * @return number of times to repeat (0 = no loop, -1 = infinite)
     */
    int getCycles();

    /**
     * Gets current playing cycle number.
     *
     * @return current cycle index (0 = first play)
     */
    int getCurrentCycle();

    /**
     * Returns current player status.
     *
     * @return current status (PLAYING, PAUSED, STOPPED, etc.)
     */
    TrackPlayerStatus getStatus();

    /**
     * Gets current playback time position.
     *
     * @return elapsed time from start of track
     */
    Duration getCurrentTime();

    /**
     * Sets playback volume in real-time.
     * Implementation may delegate to internal GainEffect.
     *
     * @param volume volume (0.0 = silent, 1.0 = normal, >1.0 = boost)
     * @throws IllegalArgumentException if volume out of valid range
     */
    void setVolume(float volume);

    /**
     * Sets playback speed multiplier.
     * Note: Speed change may affect pitch unless advanced resampling is used.
     *
     * @param speed speed (0.5 = half, 1.0 = normal, 2.0 = double)
     * @throws IllegalArgumentException if speed <= 0
     * @throws UnsupportedOperationException if speed change not supported by implementation
     */
    void setSpeed(float speed);

    /**
     * Sets loop (repeat) count.
     *
     * @param count number of times to repeat (0 = no loop, -1 = infinite)
     * @throws IllegalArgumentException if count < -1
     */
    void setLoopCount(int count);

    /**
     * Seeks to specific position in track.
     * Position is clamped to track duration.
     *
     * @param position target position to seek to
     * @throws IllegalArgumentException if position is null or negative
     * @throws IllegalStateException if player is not initialized
     */
    void seek(Duration position);

    /**
     * Switches audio output device during playback.
     * Player will pause briefly during device switch and resume automatically.
     *
     * @param newOutDevice new output device info
     * @throws NullPointerException if device is null
     * @throws IllegalArgumentException if device type is not OUTPUT
     */
    void setAudioDevice(AudioDeviceInfo newOutDevice);

    /**
     * Gets current audio device info.
     *
     * @return device information of current output device
     */
    AudioDeviceInfo getCurrentDevice();

    /**
     * Closes player and releases all native resources.
     * Player cannot be used after closing.
     * <p>
     * Safe to call multiple times.
     * </p>
     */
    @Override
    void close();
}