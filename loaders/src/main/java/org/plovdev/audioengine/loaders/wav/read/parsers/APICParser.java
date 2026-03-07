package org.plovdev.audioengine.loaders.wav.read.parsers;

import org.plovdev.audioengine.loaders.wav.chunks.APICStructure;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.plovdev.audioengine.metadata.image.ImageMimeType;
import org.plovdev.audioengine.metadata.image.ImageType;
import org.plovdev.audioengine.metadata.image.TrackImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class APICParser {

    private static final Logger log = LoggerFactory.getLogger(APICParser.class);

    public static TrackImage parseToImage(byte[] apicData) {
        if (apicData == null || apicData.length < 20) {
            log.warn("Image structure is not correct: {}", apicData);
            return null;
        }
        try {
            APICStructure structure = parseAPICStructure(apicData);

            if (structure.getBody() == null) {
                log.error("Cannot parse image structure");
                return null;
            }

            return decodeImageData(structure.getBody(), structure);
        } catch (Exception e) {
            log.error("Image parsing error: ", e);
            return null;
        }
    }

    /**
     * Парсит структуру APIC тега согласно ID3v2 спецификации.
     */
    private static APICStructure parseAPICStructure(byte[] data) {
        int pos = 0;
        int encoding = data[pos] & 0xFF;
        pos++;

        StringBuilder mimeBuilder = new StringBuilder();
        while (pos < data.length && data[pos] != 0) {
            mimeBuilder.append((char) data[pos]);
            pos++;
        }
        String mimeType = mimeBuilder.toString();
        pos++;

        int pictureType = 0;
        if (pos < data.length) {
            pictureType = data[pos] & 0xFF;
            pos++;
        }

        StringBuilder descBuilder = new StringBuilder();
        while (pos < data.length && data[pos] != 0) {
            descBuilder.append((char) data[pos]);
            pos++;
        }
        pos++;

        int imageSize = data.length - pos;
        byte[] imageData = new byte[imageSize];

        if (imageSize > 0) {
            System.arraycopy(data, pos, imageData, 0, imageSize);
        }

        return new APICStructure(WavChunkId.APIC, data.length, imageData, encoding, mimeType, pictureType, descBuilder.toString());
    }

    /**
     * Декодирует бинарные данные изображения в объект Image.
     */
    private static TrackImage decodeImageData(byte[] imageData, APICStructure structure) {
        if (imageData == null || imageData.length < 10) {
            log.error("Image data is very small.");
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            BufferedImage img = ImageIO.read(bis);
            if (img != null) {
                return new TrackImage(ImageMimeType.getFrom(structure.getMimeType()), ImageType.getFrom(structure.getPicType()), img);
            }
        } catch (IOException e) {
            log.error("Image reading error: ", e);
        }
        return null;
    }
}