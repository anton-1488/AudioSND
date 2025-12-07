package main.java.org.plovdev.audioengine.devices;

import java.nio.ByteBuffer;

public interface OutputAudioDevice extends AudioDevice {
    int write(ByteBuffer byteBuffer);
    int write(ByteBuffer byteBuffer, int start, int end);
    void flush();
}