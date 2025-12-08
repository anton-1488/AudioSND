package org.plovdev.audioengine.tracks.meta;

public class MetadataUtils {
    public static String convertId3ToReadable(String id3Key) {
        // Преобразуем ID3 ключи в читаемые названия
        return switch (id3Key) {
            case "TIT2" -> "title";
            case "TPE1" -> "artist";
            case "TALB" -> "album";
            case "TPE2" -> "album_artist";
            case "TYER" -> "year";
            case "TRCK" -> "track_number";
            case "TCON" -> "genre";
            case "TCOM" -> "composer";
            case "TEXT" -> "lyricist";
            case "TPUB" -> "publisher";
            case "TBPM" -> "bpm";
            case "TKEY" -> "key";
            case "TMOO" -> "mood";
            case "TSRC" -> "isrc";
            case "TENC" -> "encoder";
            case "TLAN" -> "language";
            case "TCOP" -> "copyright";
            case "COMM" -> "comment";
            case "TPOS" -> "disc_number";
            default -> id3Key.toLowerCase();
        };
    }

    public synchronized static TrackMetaData merge(TrackMetaData ... metadatas) {
        TrackMetaData metadata = new TrackMetaData();
        for (TrackMetaData data : metadatas) {
            metadata.getMetadataMap().putAll(data.getMetadataMap());
        }
        return metadata;
    }
}