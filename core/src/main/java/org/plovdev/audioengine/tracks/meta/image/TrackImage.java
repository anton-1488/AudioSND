package org.plovdev.audioengine.tracks.meta.image;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Обёртка для изображения в аудиофайле (например, обложка альбома).
 * <p>
 * Поля:
 * <ul>
 *   <li>{@code mimeType} — MIME‑тип изображения (например, "image/jpeg").</li>
 *   <li>{@code pictureType} — назначение изображения (по умолчанию COVER).</li>
 *   <li>{@code image} — байтовые данные изображения в виде {@link ByteBuffer}.</li>
 * </ul>
 *
 * @see ImageType
 * @see org.plovdev.audioengine.tracks.meta.TrackMetadata
 *
 * @version 1.0
 * @author Anton
 */
public record TrackImage(ImageMimeType mimeType, ImageType pictureType, BufferedImage image) {
    public TrackImage {
        Objects.requireNonNull(mimeType);
        Objects.requireNonNull(image);

        if (pictureType == null) {
            pictureType = ImageType.OTHER;
        }
    }

    @Override
    public String toString() {
        return "TrackImage{" +
                "mimeType=" + mimeType +
                ", pictureType=" + pictureType +
                ", image=" + image +
                '}';
    }
}