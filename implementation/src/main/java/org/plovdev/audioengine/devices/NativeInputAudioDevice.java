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

import static org.plovdev.audioengine.devices.AudioDeviceStatus.*;

/**
 * Класс, который обеспечивает чтение с аудио устройства.
 *
 * @version 1.0
 * @author Anton
 */
public final class NativeInputAudioDevice implements InputAudioDevice {
    private static final Logger log = LoggerFactory.getLogger(NativeInputAudioDevice.class);
    private final AudioDeviceInfo info;
    private volatile AudioDeviceStatus status = AudioDeviceStatus.UNAVAILABLE;
    private volatile boolean isInited = false;

    private Runnable onStatusChanged = () -> {
    };

    /**
     * Создает экземпляр для работы над аудио устройстовм
     * @param info устройство с которым будет работать класс.
     */
    public NativeInputAudioDevice(AudioDeviceInfo info) {
        Objects.requireNonNull(info);
        this.info = info;
    }


    /**
     * Read data from input audio device to buffer.
     *
     * @param byteBuffer buffer to read.
     * @return readed bytes.
     */
    @Override
    public int read(@NotNull ByteBuffer byteBuffer) {
        checkForInited();
        try {
            status = RUNNING;
            return _read(byteBuffer);
        } finally {
            status = OPENED;
        }
    }

    /**
     * Open audio device.
     *
     * @param format working format
     * @throws OpenAudioDeviceException when opening failed.
     */
    @Override
    public synchronized void open(TrackFormat format) throws OpenAudioDeviceException {
        if (isInited) {
            log.warn("Audio device {} already initialized.", info.id());
            return;
        }

        if (!isSupportedFormat(format)) {
            log.warn("Device {} does not support this format: {}. See supported formats in getDeviceInfo().supportedFormats()", info.id(), format);
            // если юзер хочет/знает что формат будет работать, то пусть пробует.
        }

        setStatus(OPENING);
        try {
            _open(format, info);
            isInited = true;
            setStatus(OPENED);
            log.debug("Audio device {} opened", info.id());
        } catch (Throwable e) {
            setStatus(ERROR);
            throw new OpenAudioDeviceException("Fail to open audio device: " + e.getMessage());
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
        if (!isInited) {
            log.warn("Audio device {} not initialized for closing.", info.id());
            return;
        }

        setStatus(CLOSING);
        try {
            _close();
            isInited = false;
            setStatus(CLOSED);
            log.debug("Audio device {} closed", info.id());
        } catch (Throwable e) {
            setStatus(ERROR);
            throw new CloseAudioDeviceException("Failed to close audio device: " + e.getMessage());
        }
    }

    public synchronized void setOnStatusChanged(Runnable onChange) {
        onStatusChanged = onChange;
    }

    private synchronized void setStatus(AudioDeviceStatus status) {
        if (this.status != status) {
            this.status = status;
            onStatusChanged.run();
        }
    }

    private void checkForInited() {
        if (!isInited) {
            throw new AudioDeviceException("Audio device not opened!");
        }
    }

    public boolean isInited() {
        return isInited;
    }

    public Runnable getOnStatusChanged() {
        return onStatusChanged;
    }

    @Override
    public String toString() {
        return info.toString();
    }

    private native void _open(TrackFormat format, AudioDeviceInfo info);

    private native int _read(ByteBuffer buffer);

    private native void _close();
}