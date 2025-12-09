package test.plovdev.audioengine.tracks.meta;

import org.junit.jupiter.api.Test;
import org.plovdev.audioengine.tracks.meta.MetadataEntry;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataEntryTest {
    @Test
    public void testMetadataEntry() {
        MetadataEntry entry = new MetadataEntry();
        entry.setType(String.class);
        entry.setValue("AudioSND");

        assertEquals(String.class, entry.getType());
        assertEquals("AudioSND", entry.getValue());
    }
}