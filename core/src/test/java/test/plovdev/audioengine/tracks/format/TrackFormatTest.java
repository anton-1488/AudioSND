package test.plovdev.audioengine.tracks.format;

import org.junit.jupiter.api.Test;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class TrackFormatTest {

    @Test
    public void testRecordCreation() {
        TrackFormat format = new TrackFormat( 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);

        assertEquals(2, format.channels());
        assertEquals(16, format.bitDepth());
        assertEquals(44100, format.sampleRate());
        assertTrue(format.signed());
        assertEquals(ByteOrder.LITTLE_ENDIAN, format.byteOrder());
    }

    @Test
    public void testBitRateCalculation() {
        TrackFormat format = new TrackFormat( 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);
        // 44100 * 16 * 2 = 1,411,200 bps
        assertEquals(1411200, format.bitRate());

        TrackFormat monoFormat = new TrackFormat( 1, 8, 8000, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM8);
        assertEquals(64000, monoFormat.bitRate()); // 8000 * 8 * 1
    }

    @Test
    public void testEquality() {
        TrackFormat format1 = new TrackFormat(2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);
        TrackFormat format2 = new TrackFormat(2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);
        TrackFormat format3 = new TrackFormat(2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);

        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
    }

}