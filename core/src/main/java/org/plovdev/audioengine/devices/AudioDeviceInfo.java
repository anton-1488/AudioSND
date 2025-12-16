package org.plovdev.audioengine.devices;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.Set;

public record AudioDeviceInfo(String id, String name, String vendor, Integer channels, Set<TrackFormat> supportedForamts) {
    @Override
    public @NotNull String toString() {
        return String.format("%s(%s) powered by %s. Supports data: channels: %s, formats: %s", name, id, vendor, channels, supportedForamts);
    }
}