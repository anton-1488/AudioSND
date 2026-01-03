package org.plovdev.audioengine;

import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.plovdev.audioengine.devices.NativeInputAudioDevice;
import org.plovdev.audioengine.devices.NativeOutputAudioDevice;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine();
             NativeInputAudioDevice device = new NativeInputAudioDevice(AudioDeviceManager.getInstance().getDefaultInputDevice().getDeviceInfo())) {
            TrackFormat format = WavTrackFormatFactory.wav16bitStereo48kHz();
            device.open(format);

            System.out.println("Inited");
            ByteBuffer buffer = ByteBuffer.allocateDirect((1764000)/3);
            System.out.println("Speak...");
            System.out.println("Readed: " + device.read(buffer));
            System.out.println("readed");


            System.out.println("Created track");
            Track track = new Track(buffer, Duration.ofSeconds(3), format, new TrackMetadata());

            System.out.println(buffer.limit());

            System.out.println("Exporting");
            engine.exportTrack(track, new FileOutputStream("record.wav"));
            System.out.println("Done");
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

        log.info("NOAD Delay: {}ms", end - start);

        try {
            Thread.sleep(100000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        device.close();
    }
}