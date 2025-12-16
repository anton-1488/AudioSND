package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.time.Duration;

public class Microphone implements AutoCloseable {
    private final NativeInputAudioDevice audioDevice;
    private final TrackFormat trackFormat;

    private Microphone(TrackFormat format, InputAudioDevice device) {
        trackFormat = format;
        audioDevice = new NativeInputAudioDevice(device.getDeviceInfo());
        audioDevice.open(format);
    }

    public static Microphone open(TrackFormat format) {
        return open(format, AudioDeviceManager.getInstance().getDefaultInputDevice());
    }
    public static Microphone open(TrackFormat format, InputAudioDevice device) {
        return new Microphone(format, device);
    }

    public NativeInputAudioDevice getAudioDevice() {
        return audioDevice;
    }

    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public void start() {
        _start();
    }
    public void stop() {
        _stop();
    }

    public Track getTrack() {
        stop();
        return new Track(_getReadedData(), Duration.ZERO, trackFormat, new TrackMetadata());
    }

    @Override
    public void close() {
        _close();
    }

    private native void _start();
    private native void _stop();
    private native ByteBuffer _getReadedData();

    private native void _close();
}