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

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine()) {
            TrackMixer mixer = new NativeTrackMixer();
            Track loaded = engine.loadTrack("testdata/48000/melody1.wav");
            Track generated = TrackGenerator.generateSine(WavTrackFormatFactory.wav16bitStereo96kHz(), loaded.getDuration(), Note.C2);

            mixer.addTrack(loaded);
            mixer.addTrack(generated);

            Track mixed = mixer.doMixing();

            System.out.println(mixed);

            engine.getTrackPlayer(mixed).play();

            Thread.sleep(mixed.getDuration());
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