package org.plovdev.audioengine.utils.meta;

import java.awt.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrackMetaData {
    private final Map<MetaKey, MetadataEntry> metadata = new ConcurrentHashMap<>();

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
        REPLAYGAIN_ALBUM_PEAK("REPLAYGAIN_ALBUM_PEAK", Float.class),

        // ==== Пользовательские теги ====
        CUSTOM("CUSTOM", Object.class);

        private String key;
        private Class<?> type;

        public static MetaKey custom(String key, Class<?> type) {
            MetaKey custom = MetaKey.CUSTOM;
            custom.setKey(key);
            custom.setType(type);
            return custom;
        }

        MetaKey(String key, Class<?> type) {
            this.key = key;
            this.type = type;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public Class<?> getType() {
            return type;
        }
    }

    public TrackMetaData() {
    }

    public <T> void addMetadata(MetaKey key, T value) {
        if (key == null) {
            throw new IllegalArgumentException("Metadata key cannot be null");
        }

        Class<?> type = key.getType();
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException("Value " + value + " is not of type " + type);
        }

        metadata.put(key, new MetadataEntry(value, type));
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(MetaKey key) {
        if (key == null) {
            return null;
        }
        Class<?> type = key.getType();
        MetadataEntry entry = metadata.get(key);
        if (entry == null) {
            return null;
        }
        if (!type.isAssignableFrom(entry.getType())) {
            throw new ClassCastException(String.format("Metadata %s is of type %s, not %s", key, entry.getType().getSimpleName(), type.getSimpleName()));
        }
        return (T) entry.getValue();
    }

    public int getNumData() {
        return metadata.size();
    }

    public Map<MetaKey, MetadataEntry> getMetadataMap() {
        return metadata;
    }

    public Set<MetaKey> getKeys() {
        return metadata.keySet();
    }

    // default getters

    public String getTitle() {
        return getMetadata(MetaKey.TITLE);
    }

    public String getArtist() {
        return getMetadata(MetaKey.ARTIST);
    }

    public String getAlbum() {
        return getMetadata(MetaKey.ALBUM);
    }

    public String getGenre() {
        return getMetadata(MetaKey.GENRE);
    }

    public Integer getYear() {
        return getMetadata(MetaKey.YEAR);
    }

    public Integer getTrackNumber() {
        return getMetadata(MetaKey.TRACK_NUMBER);
    }

    public Duration getDuration() {
        return getMetadata(MetaKey.DURATION);
    }

    public Integer getSampleRate() {
        return getMetadata(MetaKey.SAMPLE_RATE);
    }

    public Integer getBitDepth() {
        return getMetadata(MetaKey.BIT_DEPTH);
    }

    public Integer getChannels() {
        return getMetadata(MetaKey.CHANNELS);
    }

    public String getEncoder() {
        return getMetadata(MetaKey.ENCODER);
    }

    public String getComment() {
        return getMetadata(MetaKey.COMMENT);
    }

    public String getCopyright() {
        return getMetadata(MetaKey.COPYRIGHT);
    }

    public Path getFilePath() {
        return getMetadata(MetaKey.FILE_PATH);
    }

    public Long getFileSize() {
        return getMetadata(MetaKey.FILE_SIZE);
    }

    public String getEncoding() {
        return getMetadata(MetaKey.ENCODING);
    }

    public Integer getBitrate() {
        return getMetadata(MetaKey.BITRATE);
    }

    public Date getCreationDate() {
        return getMetadata(MetaKey.CREATION_DATE);
    }

    public Date getModificationDate() {
        return getMetadata(MetaKey.MODIFICATION_DATE);
    }

    public Float getBpm() {
        return getMetadata(MetaKey.BPM);
    }

    public String getKey() {
        return getMetadata(MetaKey.KEY);
    }

    public String getComposer() {
        return getMetadata(MetaKey.COMPOSER);
    }

    public String getLyricist() {
        return getMetadata(MetaKey.LYRICIST);
    }

    public String getPublisher() {
        return getMetadata(MetaKey.PUBLISHER);
    }

    public String getIsrc() {
        return getMetadata(MetaKey.ISRC);
    }

    public Integer getDiscNumber() {
        return getMetadata(MetaKey.DISC_NUMBER);
    }

    public Integer getDiscTotal() {
        return getMetadata(MetaKey.DISC_TOTAL);
    }

    public Integer getTrackTotal() {
        return getMetadata(MetaKey.TRACK_TOTAL);
    }

    public String getLanguage() {
        return getMetadata(MetaKey.LANGUAGE);
    }

    public String getMood() {
        return getMetadata(MetaKey.MOOD);
    }

    public String getFileFormat() {
        return getMetadata(MetaKey.FILE_FORMAT);
    }

    public String getAudioCodec() {
        return getMetadata(MetaKey.AUDIO_CODEC);
    }

    public Image getAlbumImage() {
        return getMetadata(MetaKey.ALBUM_ART);
    }
    public Image getArtistImage() {
        return getMetadata(MetaKey.ARTIST_IMAGE);
    }

    // setters


    public void setTitle(String title) {
        addMetadata(MetaKey.TITLE, title);
    }

    public void setArtist(String artist) {
        addMetadata(MetaKey.ARTIST, artist);
    }

    public void setAlbum(String album) {
        addMetadata(MetaKey.ALBUM, album);
    }

    public void setGenre(String genre) {
        addMetadata(MetaKey.GENRE, genre);
    }

    public void setYear(Integer year) {
        addMetadata(MetaKey.YEAR, year);
    }

    public void setTrackNumber(Integer trackNumber) {
        addMetadata(MetaKey.TRACK_NUMBER, trackNumber);
    }

    public void setDuration(Duration duration) {
        addMetadata(MetaKey.DURATION, duration);
    }

    public void setSampleRate(Integer sampleRate) {
        addMetadata(MetaKey.SAMPLE_RATE, sampleRate);
    }

    public void setBitDepth(Integer bitDepth) {
        addMetadata(MetaKey.BIT_DEPTH, bitDepth);
    }

    public void setChannels(Integer channels) {
        addMetadata(MetaKey.CHANNELS, channels);
    }

    public void setEncoder(String encoder) {
        addMetadata(MetaKey.ENCODER, encoder);
    }

    public void setComment(String comment) {
        addMetadata(MetaKey.COMMENT, comment);
    }

    public void setCopyright(String copyright) {
        addMetadata(MetaKey.COPYRIGHT, copyright);
    }

    public void setFilePath(Path filePath) {
        addMetadata(MetaKey.FILE_PATH, filePath);
    }

    public void setFileSize(Long fileSize) {
        addMetadata(MetaKey.FILE_SIZE, fileSize);
    }

    public void setEncoding(String encoding) {
        addMetadata(MetaKey.ENCODING, encoding);
    }

    public void setBitrate(Integer bitrate) {
        addMetadata(MetaKey.BITRATE, bitrate);
    }

    public void setCreationDate(Date creationDate) {
        addMetadata(MetaKey.CREATION_DATE, creationDate);
    }

    public void setModificationDate(Date modificationDate) {
        addMetadata(MetaKey.MODIFICATION_DATE, modificationDate);
    }

    public void setBpm(Float bpm) {
        addMetadata(MetaKey.BPM, bpm);
    }

    public void setKey(String key) {
        addMetadata(MetaKey.KEY, key);
    }

    public void setComposer(String composer) {
        addMetadata(MetaKey.COMPOSER, composer);
    }

    public void setLyricist(String lyricist) {
        addMetadata(MetaKey.LYRICIST, lyricist);
    }

    public void setPublisher(String publisher) {
        addMetadata(MetaKey.PUBLISHER, publisher);
    }

    public void setIsrc(String isrc) {
        addMetadata(MetaKey.ISRC, isrc);
    }

    public void setDiscNumber(Integer discNumber) {
        addMetadata(MetaKey.DISC_NUMBER, discNumber);
    }

    public void setDiscTotal(Integer discTotal) {
        addMetadata(MetaKey.DISC_TOTAL, discTotal);
    }

    public void setTrackTotal(Integer trackTotal) {
        addMetadata(MetaKey.TRACK_TOTAL, trackTotal);
    }

    public void setLanguage(String language) {
        addMetadata(MetaKey.LANGUAGE, language);
    }

    public void setMood(String mood) {
        addMetadata(MetaKey.MOOD, mood);
    }

    public void setFileFormat(String fileFormat) {
        addMetadata(MetaKey.FILE_FORMAT, fileFormat);
    }

    public void setAudioCodec(String audioCodec) {
        addMetadata(MetaKey.AUDIO_CODEC, audioCodec);
    }

    public void setAlbumImage(Image image) {
        addMetadata(MetaKey.ALBUM_ART, image);
    }
    public void setArtistImage(Image image) {
        addMetadata(MetaKey.ARTIST_IMAGE, image);
    }

    // helpers

    public boolean hasTitle() {
        return getTitle() != null;
    }

    public boolean hasArtist() {
        return getArtist() != null;
    }

    public boolean hasAlbum() {
        return getAlbum() != null;
    }

    public boolean hasDuration() {
        return getDuration() != null;
    }

    public boolean hasSampleRate() {
        return getSampleRate() != null;
    }

    public boolean hasBitDepth() {
        return getBitDepth() != null;
    }

    public boolean hasChannels() {
        return getChannels() != null;
    }

    // Formatting methods

    public String getFormattedDuration() {
        Duration duration = getDuration();
        if (duration == null) return "Unknown";

        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    public boolean isEmpty() {
        return metadata.isEmpty();
    }

    public void clear() {
        metadata.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TrackMetaData{");

        String title = getTitle();
        String artist = getArtist();
        String album = getAlbum();

        if (title != null) sb.append("title='").append(title).append("', ");
        if (artist != null) sb.append("artist='").append(artist).append("', ");
        if (album != null) sb.append("album='").append(album).append("', ");

        sb.append("size=").append(metadata.size()).append("}");
        return sb.toString();
    }
}