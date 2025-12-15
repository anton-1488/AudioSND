package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.devices.OutputAudioDevice;

import java.time.Duration;

public class NativeTrackPlayer implements TrackPlayer {
    private final Track track;
    private OutputAudioDevice audioDevice;

    public NativeTrackPlayer(Track track, OutputAudioDevice device) {
        this.track = track;
        audioDevice = device;
    }

    @Override
    public void initPlayer() {
        audioDevice.open(track.getFormat());
    }

    /**
     * Starts or resumes playback.
     *
     * @throws IllegalStateException if player is not prepared
     */
    @Override
    public void play() {

    }

    /**
     * Pauses playback. Playback can be resumed with {@link #play()}.
     *
     * @throws IllegalStateException if player is not playing
     */
    @Override
    public void pause() {

    }

    /**
     * Stops playback and resets position to beginning.
     * Player remains prepared and can be played again.
     *
     * @throws IllegalStateException if player is not active
     */
    @Override
    public void stop() {

    }

    /**
     * Gets current playback volume.
     */
    @Override
    public float getVolume() {
        return 0;
    }

    /**
     * Gets current playback speed.
     */
    @Override
    public float getSpeed() {
        return 0;
    }

    /**
     * Gets total cycles count.
     */
    @Override
    public int getCycles() {
        return 0;
    }

    /**
     * Gets current playing cycle.
     */
    @Override
    public int getCurrentCycle() {
        return 0;
    }

    /**
     * @return Returns current player status.
     */
    @Override
    public TrackStatus getStatus() {
        return null;
    }

    /**
     * Gets current playback time.
     */
    @Override
    public Duration getCurrentTime() {
        return null;
    }

    /**
     * Sets playback volume.
     *
     * @param volume volume (0.0 = silent, 1.0 = max)
     * @throws IllegalArgumentException if volume out of range
     */
    @Override
    public void setVolume(float volume) {

    }

    /**
     * Sets playback speed multiplier.
     *
     * @param speed speed (0.5 = half, 1.0 = normal, 2.0 = double)
     * @throws IllegalArgumentException      if speed out of range
     * @throws UnsupportedOperationException if speed change not supported
     */
    @Override
    public void setSpeed(float speed) {

    }

    /**
     * Sets loop count.
     *
     * @param count number of times to repeat (0 = no loop, -1 = infinite)
     * @throws IllegalArgumentException if count < -1
     */
    @Override
    public void setLoopCount(int count) {

    }

    /**
     * Seeks to specific position in track.
     *
     * @param position position to seek to
     * @throws IllegalArgumentException if position is out of bounds
     * @throws IllegalStateException    if player is not prepared
     */
    @Override
    public void seek(Duration position) {

    }

    /**
     * Closes player and releases all resources.
     * Player cannot be used after close.
     */
    @Override
    public void close() {
        audioDevice.close();
    }
}