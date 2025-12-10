package org.plovdev.audioengine.tracks.meta;

import java.awt.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Date;

/**
 * Enum of ID3 metadata keys.
 *
 * @see TrackMetadata
 *
 * @author Anton
 * @version 1.0
 */
public enum MetaKey {
    TITLE("TIT2", String.class),           // Название трека
    ARTIST("TPE1", String.class),          // Основной исполнитель
    ALBUM("TALB", String.class),           // Альбом
    ALBUM_ARTIST("TPE2", String.class),    // Исполнитель альбома
    YEAR("TYER", Integer.class),           // Год
    TRACK_NUMBER("TRCK", Integer.class),   // Номер трека в альбоме
    GENRE("TCON", String.class),           // Жанр

    // Дополнительные музыкальные
    COMPOSER("TCOM", String.class),        // Композитор
    LYRICIST("TEXT", String.class),        // Автор текста
    PUBLISHER("TPUB", String.class),       // Издатель
    BPM("TBPM", Float.class),              // Темп (ударов в минуту)
    KEY("TKEY", String.class),             // Музыкальный ключ
    MOOD("TMOO", String.class),            // Настроение
    ISRC("TSRC", String.class),            // Международный стандартный код записи

    // Технические
    ENCODER("TENC", String.class),         // Кодировщик
    LANGUAGE("TLAN", String.class),        // Язык
    COPYRIGHT("TCOP", String.class),       // Копирайт
    COMMENT("COMM", String.class),         // Комментарий

    // ==== Расширенные/пользовательские теги ====
    DISC_NUMBER("TPOS", Integer.class),    // Номер диска
    DISC_TOTAL("TPOS", Integer.class),     // Всего дисков (часто вместе с номером)
    TRACK_TOTAL("TRCK", Integer.class),    // Всего треков в альбоме

    // ==== Аудио-технические метаданные (не ID3, но стандартные) ====
    DURATION("DURATION", Duration.class),  // Продолжительность
    SAMPLE_RATE("SAMPLERATE", Integer.class), // Частота дискретизации
    BIT_DEPTH("BITDEPTH", Integer.class),  // Глубина бит
    CHANNELS("CHANNELS", Integer.class),   // Количество каналов
    BITRATE("BITRATE", Integer.class),     // Битрейт
    ENCODING("ENCODING", String.class),    // Тип кодирования
    FILE_FORMAT("FORMAT", String.class),   // Формат файла
    AUDIO_CODEC("CODEC", String.class),    // Аудио-кодек

    // ==== Информация о файле ====
    FILE_PATH("FILEPATH", Path.class),     // Путь к файлу
    FILE_SIZE("FILESIZE", Long.class),     // Размер файла
    CREATION_DATE("CREATED", Date.class),  // Дата создания
    MODIFICATION_DATE("MODIFIED", Date.class), // Дата изменения

    // ==== Изображения (ID3: APIC frame) ====
    ALBUM_ART("APIC", Image.class),        // Обложка альбома
    ARTIST_IMAGE("APIC", Image.class),     // Фото исполнителя

    // ==== Replay Gain (стандарт для нормализации громкости) ====
    REPLAYGAIN_TRACK_GAIN("REPLAYGAIN_TRACK_GAIN", Float.class),
    REPLAYGAIN_TRACK_PEAK("REPLAYGAIN_TRACK_PEAK", Float.class),
    REPLAYGAIN_ALBUM_GAIN("REPLAYGAIN_ALBUM_GAIN", Float.class),
    REPLAYGAIN_ALBUM_PEAK("REPLAYGAIN_ALBUM_PEAK", Float.class);

    private final String key;
    private final Class<?> type;

    MetaKey(String key, Class<?> type) {
        this.key = key;
        this.type = type;
    }
    public String getKey() {
        return key;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Field: %s, class type: %s.", MetadataUtils.convertId3ToReadable(this), type.getSimpleName());
    }
}