package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.exceptions.AudioEngineException;

import java.util.List;
import java.util.NoSuchElementException;

public class AudioDeviceManager {
    private static AudioDeviceManager INSTANSE = null;
    public static AudioDeviceManager getInstance() {
        if (INSTANSE == null) {
            INSTANSE = new AudioDeviceManager();
        }
        return INSTANSE;
    }
    public InputAudioDevice getInputDeviceById(String id) {
        for (InputAudioDevice device : getInputDevices()) {
            if (device.getDeviceInfo().id().equals(id)) {
                return device;
            }
        }
        throw new NoSuchElementException("Audio device not found");
    }

    public OutputAudioDevice getOutputDeviceById(String id) {
        for (OutputAudioDevice device : getOutputDevices()) {
            if (device.getDeviceInfo().id().equals(id)) {
                return device;
            }
        }
        throw new NoSuchElementException("Audio device not found");
    }


    public NativeInputAudioDevice getDefaultInputDevice() {
        List<InputAudioDevice> devices = getInputDevices();
        if (!devices.isEmpty()) {
            return new NativeInputAudioDevice(devices.getFirst().getDeviceInfo());
        }
        throw new AudioEngineException("Default audio device not found.");
    }

    public NativeOutputAudioDevice getDefaultOutputDevice() {
        List<OutputAudioDevice> devices = getOutputDevices();
        if (!devices.isEmpty()) {
            return new NativeOutputAudioDevice(devices.getFirst().getDeviceInfo());
        }
        throw new AudioEngineException("Default audio device not found.");
    }

    public native List<InputAudioDevice> getInputDevices();
    public native List<OutputAudioDevice> getOutputDevices();
}