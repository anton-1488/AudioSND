package org.plovdev.audioengine.profiler;

import org.plovdev.audioengine.devices.AudioDeviceInfo;

import java.util.List;

public record EngineSnapshot(long timestamp, double cpuUsage, long usedMemory,
                             long nativeMemoryUsed, int threadCount, int loadedTracks,
                             List<AudioDeviceInfo> availableAudioDevices) {

    @Override
    public String toString() {
        return "EngineSnapshot{" +
                "timestamp=" + timestamp +
                ", cpuUsage=" + cpuUsage +
                ", usedMemory=" + usedMemory +
                ", nativeMemoryUsed=" + nativeMemoryUsed +
                ", threadCount=" + threadCount +
                ", loadedTracks=" + loadedTracks +
                ", availableAudioDevices=" + availableAudioDevices +
                '}';
    }
}