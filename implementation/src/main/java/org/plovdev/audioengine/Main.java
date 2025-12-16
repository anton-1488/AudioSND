package org.plovdev.audioengine;

import org.plovdev.audioengine.devices.Microphone;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.TrackPlayer;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine();
             Microphone mic = Microphone.open(WavTrackFormatFactory.wav16bitStereo44kHz())) {
            mic.start();
            log.info("Говорите:");
            Thread.sleep(10000); // я буду говорить что-то 10 сек
            log.info("Все, не говорите.");
            Track track = mic.getTrack(); // stop под капотом.
            TrackPlayer player = engine.getTrackPlayer(track);
            player.play();
            Thread.sleep(10000); // слушаем запись
            player.close();
        } catch (Exception e) {
            log.error("Mic error: ", e);
        }
    }
}