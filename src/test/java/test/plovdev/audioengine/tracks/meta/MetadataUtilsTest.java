package test.plovdev.audioengine.tracks.meta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.plovdev.audioengine.tracks.meta.MetaKey.*;
import static org.plovdev.audioengine.tracks.meta.MetadataUtils.convertId3ToReadable;

public class MetadataUtilsTest {
    @Test
    public void testMetadataId3Converter() {
        assertEquals("title", convertId3ToReadable(TITLE));
        assertEquals("year", convertId3ToReadable(YEAR));

        assertNotEquals("album", convertId3ToReadable(TITLE));
        assertNotEquals("bpm", convertId3ToReadable(YEAR));

        // test default returns
        assertEquals("codec", convertId3ToReadable(AUDIO_CODEC));
    }
}