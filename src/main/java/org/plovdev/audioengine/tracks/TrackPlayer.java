package org.plovdev.audioengine.tracks;

import org.plovdev.audioengine.effects.TrackEqualizer;

import java.time.Duration;

public interface TrackPlayer extends AutoCloseable {
    void initPlayer();

    void play();
    void pause();
    void stop();

    float getVolume();
    float getSpeed();
    int getCycles();
    TrackStatus getStatus();
    Duration getCurrentTime();
    int getCurrentCycle();

    void setVolume(float vol);
    void setSpeed(float speed);
    void setCycles(int cycles);
    void seek(Duration toSeek);

    TrackEqualizer getEqualizer();

    @Override
    void close();
}