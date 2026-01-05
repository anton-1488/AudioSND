package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Microphone implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Microphone.class);
    private final NativeInputAudioDevice audioDevice;
    private final TrackFormat trackFormat;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final AtomicBoolean isRun = new AtomicBoolean(false);

    private final AtomicInteger readedLength = new AtomicInteger(0);
    private final AtomicInteger chunkSize = new AtomicInteger(1024);

    private boolean isInited;

    private final Queue<ByteBuffer> readedData = new ConcurrentLinkedQueue<>();
    private final Thread thread;

    private Microphone(TrackFormat format, AudioDeviceInfo info) {
        trackFormat = format;
        audioDevice = new NativeInputAudioDevice(info);
        audioDevice.open(format);
        isInited = true;
        isRun.set(true);

        thread = new Thread(this::recordLoop, "record-loop");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public static Microphone open(TrackFormat format) {
        return open(format, AudioDeviceManager.getInstance().getDefaultInputAudioDevice());
    }

    public static Microphone open(TrackFormat format, AudioDeviceInfo info) {
        return new Microphone(format, info);
    }

    public NativeInputAudioDevice getAudioDevice() {
        return audioDevice;
    }

    public TrackFormat getTrackFormat() {
        return trackFormat;
    }

    public void start() {
        checkForInited();
        isRecording.set(true);
    }

    public void stop() {
        checkForInited();
        isRecording.set(false);
    }

    public Track getTrack() {
        stop();

        ByteBuffer totalBytes = ByteBuffer.allocateDirect(readedLength.get());

        for (ByteBuffer buffer : readedData) {
            buffer.rewind();
            totalBytes.put(buffer);
        }

        totalBytes.flip();

        return new Track(totalBytes, Duration.ofMillis(TrackFormatUtils.calculateDurationMs(trackFormat, readedLength.get())), trackFormat, new TrackMetadata());
    }

    private void recordLoop() {
        checkForInited();

        while (isRun.get()) {
            if (!isRecording.get()) {
                try {
                    Thread.sleep(1);
                    continue;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            ByteBuffer readed = ByteBuffer.allocateDirect(chunkSize.get());
            int readedBytes = audioDevice.read(readed);

            readed.limit(readedBytes);
            readed.rewind();

            readedData.add(readed);
            readedLength.addAndGet(readed.remaining());
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

    public boolean isInited() {
        return isInited;
    }

    private void checkForInited() {
        if (!isInited) {
            throw new IllegalStateException("Audio device not opened");
        }
    }

    @Override
    public void close() throws InterruptedException {
        isRun.set(false);
        thread.join();

        audioDevice.close();
        readedLength.set(0);
        readedData.clear();
        isInited = false;
    }
}