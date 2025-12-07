package org.plovdev.audioengine.player;

import org.plovdev.audioengine.utils.TrackStatus;

import java.time.Duration;

public interface TrackPlayer {
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

    void closePlayer();
}