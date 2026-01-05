package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.devices.Microphone;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;

public class MicrophoneExample {
    private static final Logger log = LoggerFactory.getLogger(MicrophoneExample.class);

    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine();
             Microphone microphone = Microphone.open(WavTrackFormatFactory.wav16bitStereo44kHz())) {

            microphone.start(); // начинаем запись
            Thread.sleep(10000); // условно, ждем сколько надо.
            Track track = microphone.getTrack(); // получаем результат, и делаем что надо.

            engine.exportTrack(track, new FileOutputStream("recorded.wav"));
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }
}