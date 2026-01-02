package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.devices.NativeOutputAudioDevice;
import org.plovdev.audioengine.devices.OutputAudioDevice;
import org.plovdev.audioengine.exceptions.AudioDeviceException;
import org.plovdev.audioengine.exceptions.OpenAudioDeviceException;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class NativeTrackPlayer implements TrackPlayer {
    private static final Logger log = LoggerFactory.getLogger(NativeTrackPlayer.class);
    private final Track track;
    private final NativeOutputAudioDevice audioDevice;
    private final ByteBuffer data;
    private final AtomicInteger position = new AtomicInteger(0);
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private final AtomicBoolean isInited = new AtomicBoolean(false);
    private TrackStatus status = TrackStatus.UNAVAILABLE;
    private final int chunkSize;
    private int currentCycle = 0;
    private final int ms = 1;

    private float speed = 1.0f;
    private float volume = 0.5f;

    private int totalCycles = 1;

    private Runnable onStatusChanged = () -> {
    };

    public NativeTrackPlayer(Track track, OutputAudioDevice device) {
        this.track = track;
        audioDevice = new NativeOutputAudioDevice(device.getDeviceInfo());
        data = track.getTrackData();

        initPlayer();
        log.info("Inited");

        chunkSize = TrackFormatUtils.calculateChunkSizeInBytes(track.getFormat(), ms);
    }

    @Override
    public void initPlayer() {
        if (!isInited.get()) {
            try {
                audioDevice.open(track.getFormat());
                isInited.set(true);
                setStatus(TrackStatus.INITED);
            } catch (OpenAudioDeviceException e) {
                setStatus(TrackStatus.UNAVAILABLE);
                throw new OpenAudioDeviceException(e.getMessage());
            }
        }
    }

    /**
     * Starts or resumes playback.
     *
     * @throws IllegalStateException if player is not prepared
     */
    @Override
    public synchronized void play() {
        checkIfInited();

        if (isPlaying.get()) {
            return;
        }

        isPlaying.set(true);
        setStatus(TrackStatus.PLAYING);

        Thread thread = new Thread(this::audioLoop, "audio-loop");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    /**
     * Pauses playback. Playback can be resumed with {@link #play()}.
     *
     * @throws IllegalStateException if player is not playing
     */
    @Override
    public synchronized void pause() {
        checkIfInited();
        if (!isPlaying.get()) return;

        isPlaying.set(false);
        audioDevice.flush();
        setStatus(TrackStatus.PAUSED);
    }

    /**
     * Stops playback and resets position to beginning.
     * Player remains prepared and can be played again.
     *
     * @throws IllegalStateException if player is not active
     */
    @Override
    public synchronized void stop() {
        checkIfInited();

        isPlaying.set(false);
        position.set(0);

        audioDevice.flush();

        setStatus(TrackStatus.STOPPED);
    }

    /**
     * Gets current playback volume.
     */
    @Override
    public float getVolume() {
        return volume;
    }

    /**
     * Gets current playback speed.
     */
    @Override
    public float getSpeed() {
        return speed;
    }

    /**
     * Gets total cycles count.
     */
    @Override
    public int getCycles() {
        return totalCycles;
    }

    /**
     * Gets current playing cycle.
     */
    @Override
    public int getCurrentCycle() {
        return currentCycle;
    }

    /**
     * @return Returns current player status.
     */
    @Override
    public TrackStatus getStatus() {
        return status;
    }

    /**
     * Gets current playback time.
     */
    @Override
    public Duration getCurrentTime() {
        return Duration.ofMillis((position.get() / chunkSize) / ms);
    }

    /**
     * Sets playback volume.
     *
     * @param volume volume (0.0 = silent, 1.0 = max)
     * @throws IllegalArgumentException if volume out of range
     */
    @Override
    public void setVolume(float volume) {
        this.volume = volume;
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
        this.speed = speed;
    }

    /**
     * Sets loop count.
     *
     * @param count number of times to repeat (0 = no loop, -1 = infinite)
     * @throws IllegalArgumentException if count < -1
     */
    @Override
    public void setLoopCount(int count) {
        totalCycles = count;
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
        checkIfInited();

        int toPosition = (int) position.toMillis() * (chunkSize / ms);
        int limit = data.limit();
        if (toPosition >= limit) {
            toPosition = limit;
        }
        this.position.set(toPosition);
    }

    /**
     * Closes player and releases all resources.
     * Player cannot be used after close.
     */
    @Override
    public void close() {
        if (isInited.get()) {
            stop();
            audioDevice.close();
        }
    }

    private void checkIfInited() {
        if (!isInited.get()) {
            throw new AudioDeviceException("TrackPlayer is not ready!");
        }
    }

    public void setOnStatusChanged(Runnable onChange) {
        onStatusChanged = onChange;
    }

    private void setStatus(TrackStatus status) {
        if (this.status != status) { // Только при реальном изменении
            this.status = status;
            try {
                onStatusChanged.run();
            } catch (Exception e) {
                log.error("Error in status change callback", e);
            }
        }
    }

    private void audioLoop() {
        final int limit = data.limit();
        log.info("Start playing");

        while (isPlaying.get()) {
            int start = position.get();
            int rem = limit - start;

            if (start >= limit || rem <= 0) {
                stop();
                break;
            }


            ByteBuffer chunk = data.slice(start, Math.min(chunkSize, rem));
            audioDevice.write(chunk);

            position.set(Math.min(start + chunkSize, limit));
            LockSupport.parkNanos(700000);
        }

        currentCycle++;
        log.info("Stop playing");
        setStatus(TrackStatus.STOPPED);
    }

    public boolean isInited() {
        return isInited.get();
    }

    public Runnable getOnStatusChanged() {
        return onStatusChanged;
    }
}