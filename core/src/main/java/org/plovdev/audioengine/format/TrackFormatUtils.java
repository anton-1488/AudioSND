package org.plovdev.audioengine.format;

import org.plovdev.audioengine.format.factories.TrackFormatFactory;

import static org.plovdev.audioengine.format.factories.FlacTrackFormatFactory.flac16bitStereo44kHz;
import static org.plovdev.audioengine.format.factories.Mp3TrackFormatFactory.mp3Stereo192kbps;
import static org.plovdev.audioengine.format.factories.TrackFormatFactory.*;
import static org.plovdev.audioengine.format.factories.WavTrackFormatFactory.*;

/**
 * Utility class for common operations with {@link TrackFormat}.
 * <p>
 * Provides factory methods for quality presets, file size calculations,
 * duration estimation and chunk size determination.
 * </p>
 *
 * @author Anton
 * @version 1.0
 * @see TrackFormat
 * @see TrackFormatFactory
 */
public final class TrackFormatUtils {
    private TrackFormatUtils() {
        // Utility class, no instances
    }

    /**
     * Returns a {@link TrackFormat} for the given quality preset.
     * <p>
     * Presets cover common use cases from telephone quality to Dolby Atmos.
     * </p>
     *
     * @param preset quality preset
     * @return track format matching the preset
     */
    public static TrackFormat fromQualityPreset(QualityPreset preset) {
        return switch (preset) {
            case TELEPHONE -> telephoneMono8kHz();
            case RADIO -> wav16bitMono44kHz();
            case PODCAST -> wav16bitStereo44kHz();
            case MUSIC_MP3 -> mp3Stereo192kbps();
            case MUSIC_LOSSLESS -> flac16bitStereo44kHz();
            case GAME -> gameAudioStereo32000Hz();
            case DVD -> wav24bitStereo48kHz();
            case BLURAY -> wav24bitStereo96kHz();
            case STUDIO_MASTER -> studioMaster24bit96kHz();
            case SURROUND_51 -> surround51_24bit48kHz();
            case DOLBY_ATMOS -> dolbyAtmos_32bitFloat48kHz();
        };
    }

    /**
     * Predefined quality levels for common audio applications.
     */
    public enum QualityPreset {
        /** Telephone quality (8kHz, mono, low bitrate) */
        TELEPHONE,
        /** FM radio quality (44.1kHz, mono, 16-bit) */
        RADIO,
        /** Podcast / internet streaming quality (44.1kHz, stereo, 16-bit) */
        PODCAST,
        /** Standard MP3 music quality (192kbps stereo) */
        MUSIC_MP3,
        /** CD-quality lossless (44.1kHz, stereo, 16-bit FLAC) */
        MUSIC_LOSSLESS,
        /** Game audio (32kHz, stereo, optimized for performance) */
        GAME,
        /** DVD quality (48kHz, stereo, 24-bit) */
        DVD,
        /** Blu-ray quality (96kHz, stereo, 24-bit) */
        BLURAY,
        /** Studio master quality (96kHz, stereo, 24-bit) */
        STUDIO_MASTER,
        /** 5.1 surround sound (48kHz, 6 channels, 24-bit) */
        SURROUND_51,
        /** Dolby Atmos immersive audio (48kHz, 32-bit float) */
        DOLBY_ATMOS
    }

    /**
     * Estimates file size for the given format and duration.
     * <p>
     * For PCM formats uses sample rate, bit depth and channel count.
     * For compressed formats uses {@link TrackFormat#bitRate()} which may be
     * estimated or fixed (e.g., MP3 with constant bitrate).
     * </p>
     *
     * @param format         track format
     * @param durationSeconds duration in seconds
     * @return estimated file size in bytes
     */
    public static long calculateFileSize(TrackFormat format, long durationSeconds) {
        if (format.bitRate() > 0) {
            // For formats with known/estimated bitrate
            return (format.bitRate() * durationSeconds) / 8;
        } else {
            // For uncompressed PCM formats
            return format.sampleRate() * format.bitDepth() * format.channels() * durationSeconds / 8;
        }
    }

    /**
     * Calculates audio duration in milliseconds from file size.
     * <p>
     * Works correctly only for uncompressed PCM formats.
     * For compressed formats the result will be inaccurate.
     * </p>
     *
     * @param format       track format (must be PCM)
     * @param sizeInBytes  file size in bytes
     * @return duration in milliseconds, or 0 if calculation fails
     */
    public static long calculateDurationMs(TrackFormat format, int sizeInBytes) {
        long sampleRate = format.sampleRate();
        long bitsPerSample = format.bitDepth();
        long channels = format.channels();

        // Bytes per second = samples/sec * bytes/sample * channels
        long bytesPerSecond = (sampleRate * (bitsPerSample / 8) * channels);

        if (bytesPerSecond == 0) {
            return 0; // Avoid division by zero
        }

        // Duration in ms = (bytes * 1000) / (bytes/sec)
        return (sizeInBytes * 1000L) / bytesPerSecond;
    }

    /**
     * Returns the size of one audio frame in bytes for the given format.
     * <p>
     * One frame represents one sample across all channels.
     * </p>
     *
     * @param format track format
     * @return frame size in bytes
     */
    public static int calculateFrameSizeInBytes(TrackFormat format) {
        int bytesPerSample = format.bitDepth() / 8;
        int bytesPerFrame = bytesPerSample * format.channels();
        int framesPerMs = Math.max(1, format.sampleRate() / 1000);

        return (framesPerMs * bytesPerFrame);
    }
}