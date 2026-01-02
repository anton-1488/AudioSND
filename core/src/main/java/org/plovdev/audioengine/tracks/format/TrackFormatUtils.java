package org.plovdev.audioengine.tracks.format;

import static org.plovdev.audioengine.tracks.format.factories.FlacTrackFormatFactory.flac16bitStereo44kHz;
import static org.plovdev.audioengine.tracks.format.factories.FlacTrackFormatFactory.flac24bitStereo96kHz;
import static org.plovdev.audioengine.tracks.format.factories.Mp3TrackFormatFactory.*;
import static org.plovdev.audioengine.tracks.format.factories.TrackFormatFactory.*;
import static org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory.*;

/**
 * Provide utilities to comfort working with TrackFormat
 *
 * @author Anton
 * @version 1.0
 * @see TrackFormat
 * see also: track format factories
 */
public class TrackFormatUtils {
    private TrackFormatUtils() {
    }

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

    public static TrackFormat fromName(String formatName) {
        if (formatName == null) return null;

        return switch (formatName.toLowerCase()) {
            // WAV
            case "cd_quality", "redbook" -> wav16bitStereo44kHz();
            case "dvd_audio" -> wav24bitStereo48kHz();
            case "bluray_audio", "film" -> wav24bitStereo96kHz();
            case "studio_24_96" -> studioMaster24bit96kHz();

            // MP3
            case "mp3_low" -> mp3Stereo64kbps();
            case "mp3_medium" -> mp3Stereo128kbps();
            case "mp3_high" -> mp3Stereo192kbps();
            case "mp3_extreme" -> mp3Stereo320kbps();

            // FLAC
            case "flac_cd" -> flac16bitStereo44kHz();
            case "flac_hd" -> flac24bitStereo96kHz();

            // Профессиональные
            case "broadcast" -> wav24bitStereo48kHz();

            default -> wav16bitStereo44kHz(); // default fallback
        };
    }

    public enum QualityPreset {
        TELEPHONE,      // Телефонное качество
        RADIO,          // Радиовещание
        PODCAST,        // Подкаст/интернет
        MUSIC_MP3,      // Музыка MP3
        MUSIC_LOSSLESS, // Музыка lossless
        GAME,           // Игровое аудио
        DVD,            // DVD качество
        BLURAY,         // Blu-ray качество
        STUDIO_MASTER,  // Студийный мастер
        SURROUND_51,    // Объемный звук 5.1
        DOLBY_ATMOS     // Dolby Atmos
    }

    // ==== Методы для проверки совместимости ====

    public static boolean isCdCompatible(TrackFormat format) {
        return format.sampleRate() == 44100 &&
                format.channels() == 2 &&
                format.bitsPerSample() == 16;
    }

    public static boolean isDvdCompatible(TrackFormat format) {
        return format.sampleRate() == 48000 &&
                format.bitsPerSample() >= 16;
    }

    public static boolean isBroadcastCompatible(TrackFormat format) {
        return format.sampleRate() == 48000 &&
                format.bitsPerSample() >= 16 &&
                format.signed();
    }

    public static boolean isLossyFormat(String extension) {
        return switch (extension.toLowerCase()) {
            case "mp3", "ogg", "aac", "m4a", "opus", "wma" -> true;
            default -> false;
        };
    }

    public static boolean isLosslessFormat(String extension) {
        return switch (extension.toLowerCase()) {
            case "wav", "aiff", "flac", "alac", "ape", "wavpack" -> true;
            default -> false;
        };
    }

    public static long calculateFileSize(TrackFormat format, long durationSeconds) {
        if (format.bitRate() > 0) {
            // Для форматов с известным битрейтом
            return (format.bitRate() * durationSeconds) / 8;
        } else {
            // Для PCM форматов
            return format.sampleRate() * format.bitsPerSample() * format.channels() * durationSeconds / 8;
        }
    }

    public static long calculateDurationMs(TrackFormat format, int sizeInBytes) {
        long sampleRate = format.sampleRate();        // Гц (например, 44100)
        long bitsPerSample = format.bitsPerSample();  // бит (например, 16)
        long channels = format.channels();            // каналов (например, 2)

        // Байт в секунду = (сэмплов/сек) * (байт/сэмпл) * каналы
        long bytesPerSecond = (sampleRate * (bitsPerSample / 8) * channels);

        if (bytesPerSecond == 0) {
            return 0; // Защита от деления на ноль
        }

        // Миллисекунд = (байт * 1000) / (байт/сек)
        return (sizeInBytes * 1000L) / bytesPerSecond;
    }

    public static int calculateChunkSizeInBytes(TrackFormat f, int ms) {
        int bytesPerSample = f.bitsPerSample() / 8;
        int bytesPerFrame = bytesPerSample * f.channels();
        int framesPerMs = Math.max(1, f.sampleRate() / 1000);

        return (framesPerMs * bytesPerFrame) * ms;
    }
}