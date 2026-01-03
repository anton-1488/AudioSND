package org.plovdev.audioengine;

import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.plovdev.audioengine.devices.NativeOutputAudioDevice;
import org.plovdev.audioengine.generator.Note;
import org.plovdev.audioengine.generator.TrackGenerator;
import org.plovdev.audioengine.mixer.NativeTrackMixer;
import org.plovdev.audioengine.mixer.TrackMixer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            TrackMixer mixer = new NativeTrackMixer();
            mixer.setOutputFormat(WavTrackFormatFactory.wav16bitStereo44kHz());

            Track loaded = engine.loadTrack("testdata/48000/melody1.wav");
            Track generated = TrackGenerator.generateSine(WavTrackFormatFactory.wav16bitStereo44kHz(), Duration.ofSeconds(4), Note.C2);

            mixer.addTrack(loaded);
            mixer.addTrack(generated);

            Track mixed = mixer.doMixing();

            engine.getTrackPlayer(mixed).play();
            System.out.println(mixed.getDuration().toMillis());
            Thread.sleep(loaded.getDuration().toMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        log.info("NOAD Delay: {}ms", end-start);

        try {
            Thread.sleep(100000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        device.close();
    }
}