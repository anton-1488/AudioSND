package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Microphone implements AutoCloseable {
    private final NativeInputAudioDevice audioDevice;
    private final TrackFormat trackFormat;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final AtomicBoolean isRun = new AtomicBoolean(false);

    private final AtomicInteger readedLength = new AtomicInteger(0);
    private final AtomicInteger chunkSize = new AtomicInteger(4096);

    private final List<ByteBuffer> readedData = new CopyOnWriteArrayList<>();

    private Microphone(TrackFormat format, InputAudioDevice device) {
        trackFormat = format;
        audioDevice = new NativeInputAudioDevice(device.getDeviceInfo());
        audioDevice.open(format);
        isRun.set(true);

        Thread thread = new Thread(this::recordLoop, "record-loop");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public static Microphone open(TrackFormat format) {
        return open(format, AudioDeviceManager.getInstance().getDefaultInputDevice());
    }
    public static Microphone open(TrackFormat format, InputAudioDevice device) {
        return new Microphone(format, device);
    }

    public NativeInputAudioDevice getAudioDevice() {
        return audioDevice;
    }

    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public void start() {
        isRecording.set(true);
    }
    public void stop() {
        isRecording.set(false);
    }

    public Track getTrack() {
        stop();

        ByteBuffer totalBytes = ByteBuffer.allocateDirect(readedLength.get());
        for (ByteBuffer buffer : readedData) {
            byte[] bytes = new byte[chunkSize.get()];
            buffer.get(bytes);
            totalBytes.put(bytes);
        }

        return new Track(totalBytes, Duration.ofMillis(TrackFormatUtils.calculateDurationMs(trackFormat, readedLength.get())), trackFormat, new TrackMetadata());
    }

    private void recordLoop() {
        while (isRun.get()) {
            if (isRecording.get()) {
                ByteBuffer readed = ByteBuffer.allocateDirect(chunkSize.get());
                audioDevice.read(readed);
                readedData.add(readed);
                readedLength.addAndGet(chunkSize.get());
            }
        }
    }

    public boolean getIsRecording() {
        return isRecording.get();
    }

    public boolean getIsRun() {
        return isRun.get();
    }

    public int getReadedLength() {
        return readedLength.get();
    }

    public int getChunkSize() {
        return chunkSize.get();
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize.set(chunkSize);
    }

    @Override
    public void close() {
        isRun.set(false);
        audioDevice.close();
        readedLength.set(0);
        readedData.clear();
    }
}