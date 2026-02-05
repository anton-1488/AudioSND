package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.devices.AudioDeviceInfo;
import org.plovdev.audioengine.devices.AudioDeviceListener;
import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioDeviceListenerExample {
    private static final Logger log = LoggerFactory.getLogger(AudioDeviceListenerExample.class);

    void main() {
        try (AudioEngine engine = new NativeAudioEngine()) {
            AudioDeviceManager manager = AudioDeviceManager.getInstance();

            manager.addAudioDeviceListener(new AudioDeviceListener() {
                @Override
                public void onDeviceConnected(AudioDeviceInfo info) {
                    log.info("Device connected: {}", info);
                }

                @Override
                public void onDeviceDisconnected(AudioDeviceInfo info) {
                    log.info("Device disconnected: {}", info);
                }
            });

            Thread.sleep(10000000);
        } catch (Exception e) {
            log.error("AudioEngine error: ", e);
        }
    }
}