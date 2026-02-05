package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.devices.AudioDeviceInfo;
import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.NativeTrackPlayer;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;


public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    void main() {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.getTrackLoaderManager(WavTrackLoaderManager.class).ifPresent(m -> m.registerPathLocator(new PathLocator(Path.of("testdata/wav/48000/24"))));

            Track track = engine.loadTrack(new File("WWT.wav"));

            AudioDeviceManager manager = AudioDeviceManager.getInstance();
            List<AudioDeviceInfo> outputs = manager.getOutputAudioDevices();

            log.info("Output devices: {}", outputs);
            TrackPlayer player = new NativeTrackPlayer(track, manager.getDefaultInputAudioDevice());
            player.play();
            Thread.sleep(track.getDuration());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}