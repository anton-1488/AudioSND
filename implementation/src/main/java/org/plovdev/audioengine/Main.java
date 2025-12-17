package org.plovdev.audioengine;

import org.plovdev.audioengine.devices.AudioDeviceManager;
import org.plovdev.audioengine.devices.NativeOutputAudioDevice;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.wav.WavTrackLoaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;

public class Main extends JFrame {
    public Main() {
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500,500);

        AudioDeviceManager manager = AudioDeviceManager.getInstance();
        try {
            AudioEngine engine = new NativeAudioEngine();
            NativeOutputAudioDevice device = manager.getDefaultOutputDevice();
            engine.addLoaderManager(new WavTrackLoaderManager());

            System.out.println(device.getDeviceInfo().supportedForamts().stream().filter(f -> f.sampleRate() == 44100).toList());
            Track track1 = engine.loadTrack("test1.wav");
            Track track2 = engine.loadTrack("test2.wav");

            System.out.println(track2.getFormat());
            device.open(track2.getFormat());

            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyChar() == 'f') {
                        play(track1, device);
                    } else if (e.getKeyChar() == 'g') {
                        play(track2, device);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {

                }
            });
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        setVisible(true);
    }

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private static void play(Track track2, NativeOutputAudioDevice device) {
        long startPlay = System.currentTimeMillis();
        ByteBuffer allData = track2.getTrackData();
        device.write(allData);
        device.flush();
        long endPlay = System.currentTimeMillis();
        System.out.println("Latency: " + (endPlay - startPlay));
    }
    //#include "org_plovdev_audioengine_devices_NativeOutputAudioDevice.h"
}