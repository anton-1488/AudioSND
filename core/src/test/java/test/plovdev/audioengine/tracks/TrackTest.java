package test.plovdev.audioengine.tracks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.plovdev.audioengine.exceptions.AudioEngineException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.nio.ByteBuffer;
import java.time.Duration;

public class TrackTest {
    @Test
    public void testDirectBuffer() {
        assertThrows(AudioEngineException.class, () -> new Track(ByteBuffer.allocate(0), Duration.ofSeconds(0), WavTrackFormatFactory.wav16bitStereo44kHz(), new TrackMetadata()));
        assertDoesNotThrow(() -> new Track(ByteBuffer.allocateDirect(0), Duration.ofSeconds(0), WavTrackFormatFactory.wav16bitStereo44kHz(), new TrackMetadata()));
    }

    @Test
    void testNpe() {
        assertThrows(NullPointerException.class, () -> new Track(null, Duration.ofSeconds(0), WavTrackFormatFactory.wav16bitStereo44kHz(), new TrackMetadata()));
        assertThrows(NullPointerException.class, () -> new Track(ByteBuffer.allocateDirect(0), null, WavTrackFormatFactory.wav16bitStereo44kHz(), new TrackMetadata()));
        assertThrows(NullPointerException.class, () -> new Track(ByteBuffer.allocateDirect(0), Duration.ofMillis(0), null, new TrackMetadata()));
        assertDoesNotThrow(() -> new Track(ByteBuffer.allocateDirect(0), Duration.ofSeconds(0), WavTrackFormatFactory.wav16bitStereo44kHz(), new TrackMetadata()));
    }
}
