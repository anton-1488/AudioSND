package test.plovdev.audioengine.tracks.meta;

import org.junit.jupiter.api.Test;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TrackMetadataTest {
    @Test
    public void testPutMetadata() {
        TrackMetadata metadata = new TrackMetadata();
        long time = System.currentTimeMillis();

        metadata.setTitle("AudioSND");
        metadata.setCreationDate(new Date(time));
        metadata.setYear(2025);

        Optional<String> title = metadata.getTitle();
        Optional<Date> creationDate = metadata.getCreationDate();
        Optional<Integer> year = metadata.getYear();

        assertTrue(title.isPresent());
        assertTrue(creationDate.isPresent());
        assertTrue(year.isPresent());

        assertEquals("AudioSND", title.get());
        assertEquals(new Date(time), creationDate.get());
        assertEquals(2025, year.get());
    }

    @Test
    public void testUnrealMetadata() {
        TrackMetadata metadata = new TrackMetadata();
        metadata.setTitle("AudioSND");

        Optional<String> title = metadata.getTitle();
        Optional<Date> date = metadata.getCreationDate();

        assertTrue(title.isPresent());
        assertFalse(date.isPresent());
    }

    @Test
    public void testMetadataHelpers() {
        TrackMetadata metadata = new TrackMetadata();
        metadata.setTitle("AudioSND");

        assertEquals(1, metadata.getNumData());
        assertFalse(metadata.isEmpty());

        metadata.clear();

        assertTrue(metadata.isEmpty());
    }

    @Test
    public void testThrowNPEMetaData() {
        TrackMetadata metadata = new TrackMetadata();
        assertThrows(Exception.class, () -> metadata.setTitle(null));
    }
}