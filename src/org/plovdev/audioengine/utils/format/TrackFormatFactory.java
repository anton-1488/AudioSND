package org.plovdev.audioengine.utils.format;

import java.nio.ByteOrder;

public class TrackFormatFactory {
    public static TrackFormat wav8bitMono44kHz() {
        return new TrackFormat(
                "wav",
                1,
                8,
                44100,
                false,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav16bitMono44kHz() {
        return new TrackFormat(
                "wav",
                1,
                16,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav16bitStereo44kHz() {
        return new TrackFormat(
                "wav",
                2,
                16,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav24bitStereo44kHz() {
        return new TrackFormat(
                "wav",
                2,
                24,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav32bitFloatStereo44kHz() {
        return new TrackFormat(
                "wav",
                2,
                32,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav16bitStereo48kHz() {
        return new TrackFormat(
                "wav",
                2,
                16,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav24bitStereo48kHz() {
        return new TrackFormat(
                "wav",
                2,
                24,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav32bitFloatStereo48kHz() {
        return new TrackFormat(
                "wav",
                2,
                32,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav16bitStereo96kHz() {
        return new TrackFormat(
                "wav",
                2,
                16,
                96000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav24bitStereo96kHz() {
        return new TrackFormat(
                "wav",
                2,
                24,
                96000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat wav24bitStereo192kHz() {
        return new TrackFormat(
                "wav",
                2,
                24,
                192000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== AIFF форматы ====

    public static TrackFormat aiff16bitStereo44kHz() {
        return new TrackFormat(
                "aiff",
                2,
                16,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat aiff24bitStereo48kHz() {
        return new TrackFormat(
                "aiff",
                2,
                24,
                48000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== MP3 форматы ====

    public static TrackFormat mp3Stereo64kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,     // compressed
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat mp3Stereo128kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat mp3Stereo192kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat mp3Stereo320kbps() {
        return new TrackFormat(
                "mp3",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== FLAC форматы ====

    public static TrackFormat flac16bitStereo44kHz() {
        return new TrackFormat(
                "flac",
                2,
                16,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat flac24bitStereo96kHz() {
        return new TrackFormat(
                "flac",
                2,
                24,
                96000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== OGG/Vorbis форматы ====

    public static TrackFormat oggVorbisStereo44kHz() {
        return new TrackFormat(
                "ogg",
                2,
                0,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat oggOpusStereo48kHz() {
        return new TrackFormat(
                "opus",
                2,
                0,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== AAC форматы ====

    public static TrackFormat aacStereo128kbps() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat aacStereo256kbps() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== Профессиональные/студийные форматы ====

    public static TrackFormat studioMaster24bit96kHz() {
        return new TrackFormat(
                "wav",
                2,
                24,
                96000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat studioMaster32bitFloat192kHz() {
        return new TrackFormat(
                "wav",
                2,
                32,
                192000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat dsd64Stereo() {
        return new TrackFormat(
                "dsf",
                2,
                1,     // DSD 1-bit
                2822400, // 2.8224 MHz
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== Игровые форматы ====

    public static TrackFormat gameAudio22050Hz() {
        return new TrackFormat(
                "wav",
                1,
                16,
                22050,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat gameAudioStereo32000Hz() {
        return new TrackFormat(
                "wav",
                2,
                16,
                32000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== Телефонные/коммуникационные форматы ====

    public static TrackFormat telephoneMono8kHz() {
        return new TrackFormat(
                "wav",
                1,
                16,
                8000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat gsm6100() {
        return new TrackFormat(
                "gsm",
                1,
                0,
                8000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== Мультиканальные форматы (surround) ====

    public static TrackFormat surround51_24bit48kHz() {
        return new TrackFormat(
                "wav",
                6,     // 5.1 surround
                24,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat surround71_24bit48kHz() {
        return new TrackFormat(
                "wav",
                8,     // 7.1 surround
                24,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat dolbyAtmos_32bitFloat48kHz() {
        return new TrackFormat(
                "wav",
                12,    // Dolby Atmos bed + objects
                32,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== Утилитарные методы ====

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
            case "bluray_audio" -> wav24bitStereo96kHz();
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
            case "film" -> wav24bitStereo96kHz();

            default -> wav16bitStereo44kHz(); // default fallback
        };
    }

    public static TrackFormat createCustom(
            String extension,
            int channels,
            int bitsPerSample,
            int sampleRate,
            boolean signed,
            ByteOrder byteOrder
    ) {
        return new TrackFormat(extension, channels, bitsPerSample, sampleRate, signed, byteOrder);
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

    // ==== Методы для расчета размера ====

    public static long calculateFileSize(TrackFormat format, long durationSeconds) {
        if (format.bitRate() > 0) {
            // Для форматов с известным битрейтом
            return (format.bitRate() * durationSeconds) / 8;
        } else {
            // Для PCM форматов
            return format.sampleRate() * format.bitsPerSample() * format.channels() * durationSeconds / 8;
        }
    }

    public static String getFormatDescription(TrackFormat format) {
        String type = isLossyFormat(format.extension()) ? "Lossy" :
                isLosslessFormat(format.extension()) ? "Lossless" : "Unknown";

        return String.format("%s %s, %dHz, %d-bit, %d channels",
                type,
                format.extension().toUpperCase(),
                format.sampleRate(),
                format.bitsPerSample(),
                format.channels()
        );
    }


    // ==== RAW PCM форматы (без заголовков) ====

    public static TrackFormat raw16bitStereo44kHz() {
        return new TrackFormat(
                "raw",
                2,
                16,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat raw32bitFloatStereo96kHz() {
        return new TrackFormat(
                "raw",
                2,
                32,
                96000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== Мультимедиа форматы ====

    public static TrackFormat webmOpusStereo48kHz() {
        return new TrackFormat(
                "webm",
                2,
                0,     // compressed
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat m4aAacStereo256kbps() {
        return new TrackFormat(
                "m4a",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== Игровые движки ====

    public static TrackFormat unrealEngine() {
        return new TrackFormat(
                "wav",
                2,
                16,
                48000,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    public static TrackFormat unityEngine() {
        return new TrackFormat(
                "wav",
                2,
                16,
                44100,
                true,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== VoIP форматы ====

    public static TrackFormat sipG711Ulaw() {
        return new TrackFormat(
                "ulaw",
                1,
                8,     // G.711 μ-law
                8000,
                false, // unsigned for μ-law
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat sipG711Alaw() {
        return new TrackFormat(
                "alaw",
                1,
                8,     // G.711 A-law
                8000,
                false,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat g722_64kbps() {
        return new TrackFormat(
                "g722",
                1,
                0,
                16000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    // ==== Старые/легаси форматы ====

    public static TrackFormat midi() {
        return new TrackFormat(
                "mid",
                0,     // MIDI не имеет каналов в обычном смысле
                0,
                0,
                false,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat modTracker() {
        return new TrackFormat(
                "mod",
                4,     // 4 канала трекера
                8,
                44100,
                false,
                ByteOrder.LITTLE_ENDIAN
        );
    }

    // ==== Аудио для видео ====

    public static TrackFormat forYouTube() {
        return new TrackFormat(
                "aac",
                2,
                0,
                48000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat forTwitch() {
        return new TrackFormat(
                "aac",
                2,
                0,
                48000,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }

    public static TrackFormat forTikTok() {
        return new TrackFormat(
                "aac",
                2,
                0,
                44100,
                true,
                ByteOrder.BIG_ENDIAN
        );
    }
}