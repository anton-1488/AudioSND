package org.plovdev.audioengine.profiler;

import org.plovdev.audioengine.devices.AudioDeviceInfo;

import java.util.List;

public record EngineSnapshot(long timestamp, float cpuUsage, long usedMemory,
                             long nativeMemoryUsed, int threadCount, int loadedTracks,
                             List<AudioDeviceInfo> availableAudioDevices) {
}