package org.plovdev.audioengine.devices;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.exceptions.AudioDeviceException;
import org.plovdev.audioengine.exceptions.CloseAudioDeviceException;
import org.plovdev.audioengine.exceptions.OpenAudioDeviceException;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.plovdev.audioengine.devices.AudioDeviceStatus.*;

/**
 * Класс, который обеспечивает запись в аудио устройства.
 *
 * @version 1.0
 * @author Anton
 */
public final class NativeOutputAudioDevice implements OutputAudioDevice {
    private static final Logger log = LoggerFactory.getLogger(NativeOutputAudioDevice.class);
    private final AudioDeviceInfo info;
    private TrackFormat trackFormat;
    private volatile AudioDeviceStatus status = UNAVAILABLE;
    private final AtomicBoolean isInited = new AtomicBoolean(false);
    private Runnable onStatusChanged = () -> {
    };

    /**
     * Создает экземпляр для работы над аудио устройстовм
     * @param info устройство с которым будет работать класс.
     */
    public NativeOutputAudioDevice(@NotNull AudioDeviceInfo info) {
        this.info = info;
    }

    /**
     * Write data to audio device
     * @param byteBuffer data to write
     * @return wrote bytes
     */
    @Override
    public int write(@NotNull ByteBuffer byteBuffer) {
        checkForInited();

        if (status == ERROR || status == CLOSING) {
            throw new AudioDeviceException(String.format("Device %s is in %s state", info.id(), status));
        }

        try {
            status = RUNNING;
            return _write(byteBuffer);
        } finally {
            status = OPENED;
        }
    }

    /**
     * Flush data to audio device
     */
    @Override
    public void flush() {
        checkForInited();
        _flush();
    }

    /**
     * Open audio device.
     *
     * @param format working format
     * @throws OpenAudioDeviceException when opening failed.
     */
    @Override
    public synchronized void open(TrackFormat format) throws OpenAudioDeviceException {
        if (isInited.get()) {
            log.warn("Audio device {} already opened with format: {}", info.id(), trackFormat);
            return;
        }

        if (!isSupportedFormat(format)) {
            log.warn("Device {} does not support format: {}", info.id(), format);
        }

        try {
            setStatus(OPENING);
            trackFormat = format;
            _open(format, info);
            setStatus(OPENED);
            isInited.set(true);
            log.debug("Audio output device {} opened", info.id());
        } catch (Throwable e) {
            log.error("Initiliazing error: ", e);
            setStatus(ERROR);
            trackFormat = null;
            throw new OpenAudioDeviceException("Failed to open device");
        }
    }

    /**
     * Return all info about audio device
     *
     * @return deivce info
     */
    @Override
    public AudioDeviceInfo getDeviceInfo() {
        return info;
    }

    /**
     * Return status of audio device, which as opened, closed, etc.
     *
     * @return device status
     */
    @Override
    public AudioDeviceStatus getDeviceStatus() {
        return status;
    }

    @Override
    public synchronized void close() throws CloseAudioDeviceException {
        if (isInited.get()) {
            try {
                setStatus(CLOSING);
                _close();
                isInited.set(false);
                setStatus(CLOSED);
            } catch (Exception e) {
                log.error("Closing error: ", e);
                setStatus(ERROR);
                throw new CloseAudioDeviceException("Failed to close device");
            } finally {
                trackFormat = null;
            }
        } else {
            log.warn("Audio device {} not initialized for closing.", info.id());
        }
    }

    /**
     * Set listener, which called when audio device status changing
     * @param onChange new listener
     */
    public synchronized void setOnStatusChanged(Runnable onChange) {
        Objects.requireNonNull(onChange);
        onStatusChanged = onChange;
    }

    private synchronized void setStatus(AudioDeviceStatus status) {
        if (this.status != status) {
            this.status = status;
            try {
                onStatusChanged.run();
            } catch (Exception e) {
                log.error("Error in status change callback", e);
            }
        }
    }

    private void checkForInited() {
        if (!isInited.get()) {
            throw new AudioDeviceException("Audio device not opened!");
        }
    }

    /**
     * Return format, which audio device opened.
     * @return format
     */
    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public boolean isInited() {
        return isInited.get();
    }

    /**
     * @return status listener
     */
    public Runnable getOnStatusChanged() {
        return onStatusChanged;
    }

    @Override
    public String toString() {
        return info.toString();
    }

    //============NATIVIES============\\

    private native void _open(TrackFormat format, AudioDeviceInfo info);

    private native int _write(ByteBuffer buffer);

    private native void _flush();

    private native void _close();
}