package org.plovdev.audioengine.examples;

import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.loaders.PathLocator;
import org.plovdev.audioengine.loaders.wav.WavTrackLoaderManager;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.meta.MetaKey;
import org.plovdev.audioengine.tracks.meta.TrackMetadata;
import org.plovdev.audioengine.tracks.meta.image.ImageType;
import org.plovdev.audioengine.tracks.meta.image.TrackImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;


public class PlayerExample {
    private static final Logger log = LoggerFactory.getLogger(PlayerExample.class);

    void main() {
        try (AudioEngine engine = new NativeAudioEngine()) {
            engine.getTrackLoaderManager(WavTrackLoaderManager.class)
                    .ifPresent(m -> m.registerPathLocator(
                            new PathLocator(Path.of("testdata/wav/48000/24"))));

            fillMeta(engine);

            Track track = engine.loadTrack(new File("Cotton Eye Joy.wav"));
            System.out.println(track.getMetaData());
        } catch (Exception e) {
            log.error("Audio engine error: ", e);
        }
    }

    private void fillMeta(AudioEngine engine) throws Exception {
        Track track = engine.loadTrack(new File("cotton-doe.wav"));
        TrackMetadata metadata = track.getMetaData();
        fillCompleteMetadata(metadata);
        engine.exportTrack(track, new FileOutputStream("Cotton Eye Joy.wav"));
    }

    private void fillCompleteMetadata(TrackMetadata metadata) {
        // ==== ОСНОВНЫЕ МУЗЫКАЛЬНЫЕ ТЕГИ ====
        metadata.setTitle("Cotton Eye Joe (Eurodance Remix)");
        metadata.setArtist("Rednex");
        metadata.setAlbum("Sex & Violins");
        metadata.setGenre("Eurodance");
        metadata.setAlbumArtist("Rednex");
        metadata.setYear(new java.util.Date()); // Текущая дата
        metadata.setTrackNumber(3);
        metadata.setTrackTotal(15);
        metadata.setDiscNumber(1);
        metadata.setDiscTotal(2);

        // ==== ДОПОЛНИТЕЛЬНЫЕ МУЗЫКАЛЬНЫЕ ====
        metadata.setComposer("Rednex Band");
        metadata.setLyricist("Traditional, Rednex");
        metadata.setPublisher("PolyGram");
        metadata.setBpm(132.0f); // Темп 132 BPM
        metadata.setKey("Am"); // Тональность ля минор
        metadata.setMood("Energetic, Happy");
        metadata.setIsrc("SEAAA9500123"); // Пример ISRC кода

        try {
            TrackImage image = new TrackImage("image/jpeg", ImageType.COVER, ImageIO.read(new File("rednex.jpg")));
            metadata.setAlbumImage(image);
        } catch (IOException e) {
            log.error("Error to write image");
        }

        metadata.setEncoder("AudioSND v1.0");
        metadata.setLanguage("EN");
        metadata.setCopyright("© 1994 PolyGram International Music B.V.");
        metadata.setComment("Original 1994 Eurodance hit. Remastered in 2024.");
        metadata.setFileFormat("WAV");
        log.debug("Filled {} metadata entries", metadata.getNumData());
    }

    // Вспомогательный метод для установки любых метаданных
    private void setMetadata(TrackMetadata metadata, MetaKey key, Object value) {
        try {
            // Используем рефлексию для вызова правильного setter
            String setterName = "set" + key.name().charAt(0) +
                    key.name().substring(1).toLowerCase();

            java.lang.reflect.Method setter = TrackMetadata.class.getMethod(
                    setterName, key.getType());
            setter.invoke(metadata, value);

        } catch (Exception e) {
            log.warn("Could not set {} = {}: {}", key, value, e.getMessage());
        }
    }
}