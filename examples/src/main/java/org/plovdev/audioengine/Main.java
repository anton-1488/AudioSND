package org.plovdev.audioengine;

import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.plovdev.audioengine.devices.Microphone;
import org.plovdev.audioengine.devices.NativeOutputAudioDevice;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine();
             Microphone microphone = Microphone.open(WavTrackFormatFactory.wav16bitStereo44kHz())) {

            microphone.start();
            System.out.println("Можно говорить");
            Thread.sleep(10000);
            System.out.println("Стоп");

            Track readed = microphone.getTrack();
            engine.exportTrack(readed, new FileOutputStream("fromMicro.wav"));
        } catch (Exception e) {
            log.error("Произошла ошибка при работае с Аудио движком: ", e);
        }
    }

    private static void testNOADDelay(AudioEngine engine) {
        AudioDeviceManager manager = AudioDeviceManager.getInstance();
        NativeOutputAudioDevice device = new NativeOutputAudioDevice(manager.getDefaultOutputDevice().getDeviceInfo());
        Track track = engine.loadTrack("testdata/44100/block-story.wav");
        device.open(track.getFormat());

        long start = System.currentTimeMillis();
        device.write(track.getTrackData());
        long end = System.currentTimeMillis();
        device.flush();

        log.info("NOAD Delay: {}ms", end - start);

        try {
            Thread.sleep(100000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        device.close();
    }
}