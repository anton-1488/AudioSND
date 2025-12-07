package main.java.org.plovdev.audioengine.exceptions;

public class UnsupportedTrackFormat extends TrackLoadException {
    public UnsupportedTrackFormat(String format) {
        super("Unable to load track format: " + format);
    }
}