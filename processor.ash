ash-1.0 // magic number

ReverbEffect {
    TrackFormat originalFormat;
    byte[] originalBytes;
    init(TrackFormat format, byte[] bytes) {
        originalFormat = format;
        originalBytes = bytes;
    }

    byte[] process() {
        // processing
    }
}