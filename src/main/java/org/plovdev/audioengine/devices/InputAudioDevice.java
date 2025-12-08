package org.plovdev.audioengine.devices;

import java.nio.ByteBuffer;

public interface InputAudioDevice extends AudioDevice {
    int read(ByteBuffer byteBuffer);
    int read(ByteBuffer byteBuffer, int start, int end);
}