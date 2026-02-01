package org.plovdev.audioengine.tracks.meta.image;

/**
 * Класс который предстовляет тип картинка(для чего она используеться)
 */
public enum ImageType {
    /**
     * Альбомная картинка
     */
    COVER(0);

    private final int type;
    ImageType(int type) {
        this.type = type;
    }

    /**
     * Ищет тип картинки по номеру
     * @param type номер типа
     * @return тип картинки
     */
    public static ImageType getFrom(int type) {
        for (ImageType t : values()) {
            if (t.getType() == type) {
                return t;
            }
        }
        return COVER; // fallback
    }

    /**
     * Возвращает номер типа картнки
     * @return номер типа
     */
    public int getType() {
        return type;
    }
}
