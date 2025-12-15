package org.plovdev.audioengine.device;

import org.plovdev.audioengine.devices.InputAudioDevice;
import org.plovdev.audioengine.devices.OutputAudioDevice;

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
            if (device.getDeviceId().equals(id)) {
                return device;
            }
        }
        throw new NoSuchElementException("Audio device not found");
    }

    public OutputAudioDevice getOutputDeviceById(String id) {
        for (OutputAudioDevice device : getOutputDevices()) {
            if (device.getDeviceId().equals(id)) {
                return device;
            }
        }
        throw new NoSuchElementException("Audio device not found");
    }


    public native InputAudioDevice getDefaultInputDevice();
    public native OutputAudioDevice getDefaultOutputDevice();

    public native List<InputAudioDevice> getInputDevices();
    public native List<OutputAudioDevice> getOutputDevices();
}