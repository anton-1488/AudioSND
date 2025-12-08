package org.plovdev.audioengine.tracks.format.factories;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import static org.plovdev.audioengine.tracks.format.factories.TrackFormatFactory.*;
import static org.plovdev.audioengine.tracks.format.factories.WavTrackFormatFactory.*;
import static org.plovdev.audioengine.tracks.format.factories.Mp3TrackFormatFactory.*;
import static org.plovdev.audioengine.tracks.format.factories.FlacTrackFormatFactory.*;

public class TrackFormatUtils {
    private TrackFormatUtils() {}

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
}