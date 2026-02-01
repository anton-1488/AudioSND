package org.plovdev.audioengine.tracks.meta.image;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Класс обертка над картинками в аудио файле. Используеться для хранения картинки и информации о ней.
 * @param mimeType mime type изображения.
 * @param type типа картинки(для чего это изображение).
 * @param image сама картинка, готовая к использованию.
 */
public record TrackImage(String mimeType, ImageType type, BufferedImage image) {
    public TrackImage {
        Objects.requireNonNull(mimeType);
        Objects.requireNonNull(image);
        if (type == null) {
            type = ImageType.COVER;
        }
    }

    @Override
    public String toString() {
        return image.toString();
    }
}