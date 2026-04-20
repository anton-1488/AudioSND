package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.devices.*;
import org.plovdev.audioengine.effects.OverdriveEffect;
import org.plovdev.audioengine.profiler.DefaultEngineProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuitarSimpleExample {
    private static final Logger log = LoggerFactory.getLogger(GuitarSimpleExample.class);
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);

    void main() {
        runStopThread();

        try (AudioEngine engine = new NativeAudioEngine()) {
            AudioDeviceManager manager = AudioDeviceManager.getInstance();
            AudioDeviceInfo inputInfo = manager.getDefaultInputAudioDevice();
            AudioDeviceInfo outputInfo = manager.getDefaultOutputAudioDevice();
            OverdriveEffect effect = new OverdriveEffect();

            effect.setup(outputInfo.supportedFormats().getFirst());
            log.info("Available devices: {}", new DefaultEngineProfiler(engine).snapshot().availableAudioDevices());

            try (InputAudioDevice input = new NativeInputAudioDevice(inputInfo);
                 OutputAudioDevice output = new NativeOutputAudioDevice(outputInfo)) {
                input.open(inputInfo.supportedFormats().getFirst());
                output.open(outputInfo.supportedFormats().getFirst());

                log.info("Start cycle: isRunning: {}", isRunning.get());
                while (isRunning.get()) {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                    input.read(buffer);
                    buffer = effect.process(buffer);
                    output.write(buffer);
                }
            }
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }

    private void runStopThread() {
        Thread.startVirtualThread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                String stop = scanner.nextLine();
                if (stop.equals("stop")) {
                    isRunning.set(false);
                }
            } catch (Exception e) {
                log.error("Stopping error: ", e);
            }
        });
    }
}