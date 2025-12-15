package org.plovdev.audioengine.devices;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.Set;

public record AudioDeviceInfo(String id, String name, String vendor, String version, int channels, Set<TrackFormat> supportedForamts) {
}