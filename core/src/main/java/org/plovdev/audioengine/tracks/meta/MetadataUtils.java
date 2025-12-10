package org.plovdev.audioengine.tracks.meta;

/**
 * Utilities for extension work with metadata.
 *
 * @see TrackMetadata
 *
 * @author Anton
 * @version 1.0
 */
public class MetadataUtils {
    public static String convertId3ToReadable(MetaKey id3Key) {
        return switch (id3Key.getKey()) {
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
            default -> id3Key.getKey().toLowerCase();
        };
    }
}