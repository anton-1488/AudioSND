package test.plovdev.audioengine.tracks.format;

import org.junit.jupiter.api.Test;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class TrackFormatTest {

    @Test
    public void testRecordCreation() {
        TrackFormat format = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);

        assertEquals("wav", format.extension());
        assertEquals(2, format.channels());
        assertEquals(16, format.bitsPerSample());
        assertEquals(44100, format.sampleRate());
        assertTrue(format.signed());
        assertEquals(ByteOrder.LITTLE_ENDIAN, format.byteOrder());
    }

    @Test
    public void testBitRateCalculation() {
        TrackFormat format = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        // 44100 * 16 * 2 = 1,411,200 bps
        assertEquals(1411200, format.bitRate());

        TrackFormat monoFormat = new TrackFormat("wav", 1, 8, 8000, true, ByteOrder.LITTLE_ENDIAN);
        assertEquals(64000, monoFormat.bitRate()); // 8000 * 8 * 1
    }

    @Test
    public void testEquality() {
        TrackFormat format1 = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        TrackFormat format2 = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        TrackFormat format3 = new TrackFormat("mp3", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);

        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
        assertNotEquals(format1, format3);
    }

    @Test
    public void testToString() {
        TrackFormat format = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        String result = format.toString();

        assertTrue(result.contains("wav"));
        assertTrue(result.contains("44100Hz"));
        assertTrue(result.contains("2ch"));
        assertTrue(result.contains("16bit"));
        assertTrue(result.contains("signed"));
        assertTrue(result.contains("LE"));
    }
}