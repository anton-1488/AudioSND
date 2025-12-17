package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.exceptions.AudioDeviceException;
import org.plovdev.audioengine.exceptions.CloseAudioDeviceException;
import org.plovdev.audioengine.exceptions.OpenAudioDeviceException;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NativeOutputAudioDevice implements OutputAudioDevice {
    private static final Logger log = LoggerFactory.getLogger(NativeOutputAudioDevice.class);
    private final AudioDeviceInfo info;
    private TrackFormat trackFormat;
    private volatile AudioDeviceStatus status = AudioDeviceStatus.UNAVAILABLE;
    private final AtomicBoolean isInited = new AtomicBoolean(false);
    private long nativeHandle = 0;
    private Runnable onStatusChanged = () -> {
    };

    public NativeOutputAudioDevice(AudioDeviceInfo info) {
        this.info = info;
    }

    @Override
    public int write(ByteBuffer byteBuffer) {
        checkForInited();

        if (status == AudioDeviceStatus.ERROR || status == AudioDeviceStatus.CLOSING) {
            throw new AudioDeviceException(
                    String.format("Device is in %s state", status)
            );
        }

        status = AudioDeviceStatus.RUNNING;
        return _write(byteBuffer);
    }

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
    public void open(TrackFormat format) throws OpenAudioDeviceException {
        if (isInited.get()) {
            log.warn("Device already opened with format: {}", trackFormat);
            return;
        }

        if (!isSupportedFormat(format)) {
            throw new OpenAudioDeviceException(
                    String.format("Format %s is not supported by device %s", format, info.name())
            );
        }

        try {
            setStatus(AudioDeviceStatus.OPENING);
            trackFormat = format;
            _open(info.id(), format);
            setStatus(AudioDeviceStatus.OPENED);
            isInited.set(true);
        } catch (Throwable e) {
            log.error("Initiliazing error: ", e);
            setStatus(AudioDeviceStatus.ERROR);
            trackFormat = null;
            nativeHandle = 0;
            throw new OpenAudioDeviceException("Failed to open device");
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
        return info.supportedForamts().contains(format);
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
        if (isInited.get()) {
            try {
                setStatus(AudioDeviceStatus.CLOSING);
                _close(info.id());
                isInited.set(false);
            } catch (Exception e) {
                log.error("Closing error: ", e);
                setStatus(AudioDeviceStatus.ERROR);
                throw new CloseAudioDeviceException("Failed to close device");
            } finally {
                nativeHandle = 0;
                trackFormat = null;
            }
        }
    }

    public void setOnStatusChanged(Runnable onChange) {
        onStatusChanged = onChange;
    }

    private void setStatus(AudioDeviceStatus status) {
        if (this.status != status) { // Только при реальном изменении
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

    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public boolean isInited() {
        return isInited.get();
    }

    public Runnable getOnStatusChanged() {
        return onStatusChanged;
    }

    @Override
    public String toString() {
        return info.toString();
    }

    private native void _open(String id, TrackFormat format);

    private native int _write(ByteBuffer buffer);

    private native void _flush();

    private native void _close(String id);
}