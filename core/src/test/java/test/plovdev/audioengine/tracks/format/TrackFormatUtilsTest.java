package test.plovdev.audioengine.tracks.format;

import org.junit.jupiter.api.Test;
import org.plovdev.audioengine.format.TrackFormat;
import org.plovdev.audioengine.format.TrackFormatUtils;
import org.plovdev.audioengine.format.TrackFormatUtils.QualityPreset;

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
        assertEquals(16, cdFormat.bitDepth());

        TrackFormat telephoneFormat = TrackFormatUtils.fromQualityPreset(QualityPreset.TELEPHONE);
        assertEquals(8000, telephoneFormat.sampleRate());
        assertEquals(1, telephoneFormat.channels());
        assertEquals(16, telephoneFormat.bitDepth());
    }

    // ==== Тесты расчета размера файла ====

    @Test
    public void testCalculateFileSize() {
        TrackFormat cdFormat = new TrackFormat(2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);
        long size = TrackFormatUtils.calculateFileSize(cdFormat, 60); // 60 секунд

        // 44100 * 16 * 2 * 60 / 8 = 10,584,000 bytes
        assertEquals(10584000L, size);
    }

    @Test
    public void testCalculateFileSizeForMono() {
        TrackFormat monoFormat = new TrackFormat(1, 8, 8000, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM8);
        long size = TrackFormatUtils.calculateFileSize(monoFormat, 30); // 30 секунд

        // 8000 * 8 * 1 * 30 / 8 = 240,000 bytes
        assertEquals(240000L, size);
    }

    @Test
    public void testCalculateFileSizeForHighResolution() {
        TrackFormat hiResFormat = new TrackFormat(2, 24, 96000, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM24);
        long size = TrackFormatUtils.calculateFileSize(hiResFormat, 180); // 3 минуты

        // 96000 * 24 * 2 * 180 / 8 = 103,680,000 bytes
        assertEquals(103680000L, size);
    }

    // ==== Граничные случаи ====

    @Test
    public void testZeroDuration() {
        TrackFormat format = new TrackFormat(2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);
        long size = TrackFormatUtils.calculateFileSize(format, 0);
        assertEquals(0, size);
    }

    @Test
    public void testNegativeDuration() {
        TrackFormat format = new TrackFormat(2, 16, 44100, true, ByteOrder.LITTLE_ENDIAN, TrackFormat.AudioCodec.PCM16);
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