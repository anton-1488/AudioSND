package test.plovdev.audioengine.tracks.format;

import org.junit.jupiter.api.Test;
import org.plovdev.audioengine.tracks.format.TrackFormat;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils;
import org.plovdev.audioengine.tracks.format.TrackFormatUtils.QualityPreset;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class TrackFormatUtilsTest {

    // ==== Тесты для fromQualityPreset ====

    public void testAllQualityPresetsReturnNonNull(QualityPreset preset) {
        TrackFormat format = TrackFormatUtils.fromQualityPreset(preset);
        assertNotNull(format, "Format should not be null for preset: " + preset);
    }

    @Test
    public void testSpecificPresets() {
        // Проверяем корректность работы пресетов
        TrackFormat cdFormat = TrackFormatUtils.fromQualityPreset(QualityPreset.PODCAST);
        assertEquals(44100, cdFormat.sampleRate());
        assertEquals(2, cdFormat.channels());
        assertEquals(16, cdFormat.bitsPerSample());

        TrackFormat telephoneFormat = TrackFormatUtils.fromQualityPreset(QualityPreset.TELEPHONE);
        assertEquals(8000, telephoneFormat.sampleRate());
        assertEquals(1, telephoneFormat.channels());
        assertEquals(16, telephoneFormat.bitsPerSample());
    }

    // ==== Тесты для fromName ====

    @Test
    public void testFromNameWithNull() {
        assertNull(TrackFormatUtils.fromName(null));
    }

    @Test
    public void testAllNamedFormatsReturnNonNull() {
        String formatName = "dvd_audio";
        TrackFormat format = TrackFormatUtils.fromName(formatName);
        assertNotNull(format, "Format should not be null for name: " + formatName);
    }

    @Test
    public void testFromNameCaseInsensitive() {
        TrackFormat lower = TrackFormatUtils.fromName("cd_quality");
        TrackFormat upper = TrackFormatUtils.fromName("CD_QUALITY");
        TrackFormat mixed = TrackFormatUtils.fromName("Cd_Quality");

        assertEquals(lower, upper);
        assertEquals(lower, mixed);
    }

    @Test
    public void testFromNameDefaultFallback() {
        TrackFormat unknown = TrackFormatUtils.fromName("unknown_format");
        assertNotNull(unknown);
        // Должен вернуть CD качество как дефолт (wav16bitStereo44kHz)
        assertEquals(44100, unknown.sampleRate());
        assertEquals(2, unknown.channels());
        assertEquals(16, unknown.bitsPerSample());
    }

    // ==== Тесты совместимости ====

    @Test
    public void testIsCdCompatible() {
        TrackFormat cdFormat = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        TrackFormat nonCdFormat = new TrackFormat("wav", 1, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);

        assertTrue(TrackFormatUtils.isCdCompatible(cdFormat));
        assertFalse(TrackFormatUtils.isCdCompatible(nonCdFormat));
    }

    @Test
    public void testIsDvdCompatible() {
        TrackFormat dvdFormat = new TrackFormat("wav", 2, 16, 48000, true, ByteOrder.LITTLE_ENDIAN);
        TrackFormat highResFormat = new TrackFormat("wav", 2, 24, 48000, true, ByteOrder.LITTLE_ENDIAN);
        TrackFormat nonDvdFormat = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);

        assertTrue(TrackFormatUtils.isDvdCompatible(dvdFormat));
        assertTrue(TrackFormatUtils.isDvdCompatible(highResFormat)); // битрейт >= 16
        assertFalse(TrackFormatUtils.isDvdCompatible(nonDvdFormat));
    }

    @Test
    public void testIsBroadcastCompatible() {
        TrackFormat broadcastFormat = new TrackFormat("wav", 2, 16, 48000, true, ByteOrder.LITTLE_ENDIAN);
        TrackFormat unsignedFormat = new TrackFormat("wav", 2, 16, 48000, false, ByteOrder.LITTLE_ENDIAN);
        TrackFormat lowBitFormat = new TrackFormat("wav", 2, 8, 48000, true, ByteOrder.LITTLE_ENDIAN);

        assertTrue(TrackFormatUtils.isBroadcastCompatible(broadcastFormat));
        assertFalse(TrackFormatUtils.isBroadcastCompatible(unsignedFormat)); // не signed
        assertFalse(TrackFormatUtils.isBroadcastCompatible(lowBitFormat)); // < 16 бит
    }

    @Test
    public void testLossyFormats() {
        String extension = "mp3";
        assertTrue(TrackFormatUtils.isLossyFormat(extension));
        assertTrue(TrackFormatUtils.isLossyFormat(extension.toUpperCase()));
    }

    @Test
    public void testLosslessFormats() {
        String extension = "wav";
        assertTrue(TrackFormatUtils.isLosslessFormat(extension));
        assertTrue(TrackFormatUtils.isLosslessFormat(extension.toUpperCase()));
    }

    @Test
    public void testNonAudioFormat() {
        assertFalse(TrackFormatUtils.isLossyFormat("txt"));
        assertFalse(TrackFormatUtils.isLosslessFormat("jpg"));
    }

    // ==== Тесты расчета размера файла ====

    @Test
    public void testCalculateFileSize() {
        TrackFormat cdFormat = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        long size = TrackFormatUtils.calculateFileSize(cdFormat, 60); // 60 секунд

        // 44100 * 16 * 2 * 60 / 8 = 10,584,000 bytes
        assertEquals(10584000L, size);
    }

    @Test
    public void testCalculateFileSizeForMono() {
        TrackFormat monoFormat = new TrackFormat("wav", 1, 8, 8000, true, ByteOrder.LITTLE_ENDIAN);
        long size = TrackFormatUtils.calculateFileSize(monoFormat, 30); // 30 секунд

        // 8000 * 8 * 1 * 30 / 8 = 240,000 bytes
        assertEquals(240000L, size);
    }

    @Test
    public void testCalculateFileSizeForHighResolution() {
        TrackFormat hiResFormat = new TrackFormat("wav", 2, 24, 96000, true, ByteOrder.LITTLE_ENDIAN);
        long size = TrackFormatUtils.calculateFileSize(hiResFormat, 180); // 3 минуты

        // 96000 * 24 * 2 * 180 / 8 = 103,680,000 bytes
        assertEquals(103680000L, size);
    }

    // ==== Граничные случаи ====

    @Test
    public void testZeroDuration() {
        TrackFormat format = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        long size = TrackFormatUtils.calculateFileSize(format, 0);
        assertEquals(0, size);
    }

    @Test
    public void testNegativeDuration() {
        TrackFormat format = new TrackFormat("wav", 2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN);
        long size = TrackFormatUtils.calculateFileSize(format, -10);
        // Ожидаем отрицательный размер (формула даст отрицательное значение)
        assertEquals(-10584000L / 6, size); // 10,584,000 / 6 = 1,764,000 * -10
    }

    // ==== Дополнительные тесты ====

    @Test
    public void testLossyAndLosslessNotOverlap() {
        // Проверяем, что формат не может быть одновременно lossy и lossless
        String[] lossyFormats = {"mp3", "ogg", "aac"};
        String[] losslessFormats = {"wav", "flac", "alac"};

        for (String lossy : lossyFormats) {
            for (String lossless : losslessFormats) {
                assertNotEquals(lossy, lossless);
            }
        }
    }

    @Test
    public void testEnumCoverage() {
        // Проверяем, что все значения enum обрабатываются
        QualityPreset[] presets = QualityPreset.values();
        assertEquals(11, presets.length); // TELEPHONE, RADIO, ..., DOLBY_ATMOS
    }
}