package test.plovdev.audioengine.tracks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions.*;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.TrackDecoder;
import org.plovdev.audioengine.loaders.wav.WavTrackDecoder;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.NativeTrackPlayer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static test.plovdev.audioengine.tracks.TestFormatName.*;

public class NativeTrackPlayingTest {
    private static final Logger log = LoggerFactory.getLogger(NativeTrackPlayingTest.class);
    private final AudioEngine engine;
    private final AudioDeviceManager manager;

    public NativeTrackPlayingTest() {
        try {
            this.engine = new NativeAudioEngine();
            engine.getTrackLoaderManager(WavTrackLoaderManager.class).ifPresent(m -> m.registerPathLocator(new PathLocator(Path.of("testdata/wav/test"))));

            this.manager = AudioDeviceManager.getInstance();
        } catch (Exception e) {
            log.error("Error to init audio engine: ", e);
            throw new RuntimeException(e);
        }
    }

    private void logTrackInfo(Track track) {
        log.info("--------------------------{}--------------------------", track.getMetaData().getTitle().orElse(""));
        log.info("Track format: {}", track.getFormat());
        log.info("Track metadata: {}", track.getMetaData());
        log.info("Duration: {}\n\n", track.getDuration().toSeconds());
    }

    private void testPlaying(TestFormatName formatName) throws Exception {
        Track track = engine.loadTrack(new File("ph-" + formatName));
        logTrackInfo(track);
        TrackDecoder decoder = new WavTrackDecoder();
        TrackFormat src = track.getFormat();
        track = decoder.decode(track, new TrackFormat(src.channels(), src.bitDepth(), src.sampleRate(), src.signed(), src.byteOrder(), TrackFormat.AudioCodec.PCM8));
        try (TrackPlayer player = new NativeTrackPlayer(track, manager.getDefaultOutputAudioDevice())) {
            player.play();
            Thread.sleep(track.getDuration().toMillis() + 1000);
        }
    }

    @Test
    void playPCM_8_U_LE() {
        assertDoesNotThrow(() -> testPlaying(PCM_8_U_LE));
    }
    @Test
    void playPCM_32_FLOAT_S_LE() {
        assertDoesNotThrow(() -> testPlaying(PCM_32_FLOAT));
    }
    @Test
    void playPCM_ULAW_S_LE() {
        assertDoesNotThrow(() -> testPlaying(PCM_ULAW));
    }
//    @Test
//    void playPCM_ALAW_S_LE() {
//        assertDoesNotThrow(() -> testPlaying(PCM_ALAW));
//    }
//    @Test
//    void playPCM_IMA_ADPCM_S_LE() {
//        assertDoesNotThrow(() -> testPlaying(PCM_IMA_ADPCM));
//    }
//    @Test
//    void playPCM_MICROSOFT_ADPCM_S_LE() {
//        assertDoesNotThrow(() -> testPlaying(PCM_MICROSOFT_ADPCM));
//    }
//    @Test
//    void playPCM_GSM_6_S_LE() {
//        assertDoesNotThrow(() -> testPlaying(PCM_GSM_6));
//    }
}