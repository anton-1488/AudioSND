package org.plovdev.audioengine.metadata.image;

/**
 * Класс который предстовляет тип картинка(для чего она используеться)
 */
public enum ImageType {
    OTHER(0x00),
    FILE_ICON_32X32(0x01),
    OTHER_FILE_ICON(0x02),
    COVER_FRONT(0x03),
    COVER_BACK(0x04),
    LEAFLET_PAGE(0x05),
    MEDIA(0x06),
    LEAD_ARTIST(0x07);

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
        return null;
    }

    /**
     * Возвращает номер типа картнки
     * @return номер типа
     */
    public int getType() {
        return type;
    }
}
