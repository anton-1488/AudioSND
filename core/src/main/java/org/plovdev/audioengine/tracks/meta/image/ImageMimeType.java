package org.plovdev.audioengine.tracks.meta.image;

/**
 * Класс для описания mime type картинок(для избежания некорректных данных)
 */
public enum ImageMimeType {
    JPEG("JPEG"), PNG("PNG"); // and other

    private final String type;
    ImageMimeType(String type) {
        this.type = type;
    }

    /**
     * Превращает строку в enum
     * @param type raw mime type(String)
     * @return mime type для картинки
     */
    public static ImageMimeType getFrom(String type) {
        for (ImageMimeType t : values()) {
            if (t.toString().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Возвращает расширение изображения
     * @return extension
     */
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "image/" + type.toLowerCase();
    }
}
