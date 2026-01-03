package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.exceptions.AudioDeviceException;
import org.plovdev.audioengine.exceptions.CloseAudioDeviceException;
import org.plovdev.audioengine.exceptions.OpenAudioDeviceException;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.plovdev.audioengine.devices.AudioDeviceStatus.*;

public final class NativeInputAudioDevice implements InputAudioDevice {
    private static final Logger log = LoggerFactory.getLogger(NativeInputAudioDevice.class);
    private final AudioDeviceInfo info;
    private volatile AudioDeviceStatus status = AudioDeviceStatus.UNAVAILABLE;
    private volatile boolean isInited = false;

    private Runnable onStatusChanged = () -> {
    };

    public NativeInputAudioDevice(AudioDeviceInfo info) {
        this.info = info;
    }

    @Override
    public int read(ByteBuffer byteBuffer) {
        checkForInited();
        status = RUNNING;
        return _read(byteBuffer);
    }

    /**
     * Open audio device.
     *
     * @param format working format
     * @throws OpenAudioDeviceException when opening failed.
     */
    @Override
    public void open(TrackFormat format) throws OpenAudioDeviceException {
        if (isInited) {
            log.warn("Audio device already inited.");
            return;
        }

        setStatus(OPENING);
        try {
            _open(format, info);
            isInited = true;
            setStatus(OPENED);
        } catch (Throwable e) {
            setStatus(ERROR);
            throw new OpenAudioDeviceException("Fail to open audio device: " + e.getMessage());
        }
    }

    /**
     * Check, supported audio device this fromat?
     *
     * @param format checking format.
     * @return is supported
     */
    @Override
    public boolean isSupportedFormat(TrackFormat format) {
        return getDeviceInfo().supportedForamts().contains(format);
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
    public void close() throws CloseAudioDeviceException {
        if (!isInited) {
            log.warn("Audio Device not inited for close.");
            return;
        }

        setStatus(CLOSING);
        try {
            _close();
            isInited = false;
            setStatus(CLOSED);
        } catch (Throwable e) {
            status = DESTROYED;
            throw new CloseAudioDeviceException("Failed to close audio device: " + e.getMessage());
        }
    }

    public void setOnStatusChanged(Runnable onChange) {
        onStatusChanged = onChange;
    }

    private void setStatus(AudioDeviceStatus status) {
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