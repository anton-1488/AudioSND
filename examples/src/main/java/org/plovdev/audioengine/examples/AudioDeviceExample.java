package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.devices.AudioDeviceInfo;
import org.plovdev.audioengine.devices.AudioDeviceListener;
import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class AudioDeviceExample {
    private static final Logger log = LoggerFactory.getLogger(AudioDeviceExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            AudioDeviceManager manager = AudioDeviceManager.getInstance();

            Thread thread = new Thread(() -> manager.addAudioDeviceListener(new AudioDeviceListener() {
                @Override
                public void onDeviceConnected(AudioDeviceInfo info) {
                    log.info("Device connected: {}", info);
                }

                @Override
                public void onDeviceDisconnected(AudioDeviceInfo info) {
                    log.info("Device disconnected: {}", info);
                }
            }), "thread");
            thread.setDaemon(true);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();

            testExecutionDelay(manager::getInputAudioDevices);
            testExecutionDelay(manager::getOutputAudioDevices);
            log.info("");
            testExecutionDelay(manager::getDefaultInputAudioDevice);
            testExecutionDelay(manager::getDefaultOutputAudioDevice);
            log.info("\n\n");

            Thread.sleep(10000000);
        } catch (Exception e) {
            log.error("AudioEngine error: ", e);
        }
    }

    private static <V> void testExecutionDelay(Callable<V> runnable) {
        try {
            long start = System.currentTimeMillis();
            V exec = runnable.call();
            long end = System.currentTimeMillis();

            log.info("Execution test delay({}ms): {}", (end - start), exec);
        } catch (Exception e) {
            log.error("Testing error: ", e);
        }
    }
}