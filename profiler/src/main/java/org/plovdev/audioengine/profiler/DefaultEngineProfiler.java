package org.plovdev.audioengine.profiler;

import com.sun.management.OperatingSystemMXBean;
import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.devices.AudioDeviceInfo;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultEngineProfiler implements AudioEngineProfiler {
    private final AudioEngine engine;
    public DefaultEngineProfiler(AudioEngine prof) {
        engine = prof;
    }

    @Override
    public EngineSnapshot snapshot() {
        Objects.requireNonNull(engine);

        OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long timestamp = System.currentTimeMillis();
        double cpuUsage = mxBean.getProcessCpuLoad();

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.maxMemory() - runtime.freeMemory();

        long nativeMemoryUsed = mxBean.getCommittedVirtualMemorySize();
        int threads = Thread.activeCount();

        List<AudioDeviceInfo> infos = new ArrayList<>();
        infos.addAll(engine.getAvailableInputAudioDevices());
        infos.addAll(engine.getAvailableOutputAudioDevices());

        return new EngineSnapshot(timestamp, cpuUsage, usedMemory, nativeMemoryUsed, threads, LoadedTrakCounter.getTracksCount(), infos);
    }

    @Override
    public long executionTime(Runnable r) {
        long startExec = System.nanoTime();
        r.run();
        return System.nanoTime() - startExec; // nano sec
    }
}