package org.plovdev.audioengine.loaders.wav.read;

import org.plovdev.audioengine.loaders.ExportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ParseUtils {
    private static final Logger log = LoggerFactory.getLogger(ParseUtils.class);

    public static int readInt(InputStream inputStream, int size) throws IOException {
        byte[] bytes = new byte[size];
        int r = inputStream.read(bytes);
        if (r != size) throw new IOException("Недостаточно данных для чтения int");
        return ExportUtils.bytesToInt(bytes, 0, size);
    }

    public static String readString(InputStream inputStream, int size) throws IOException {
        byte[] bytes = new byte[size];
        int r = inputStream.read(bytes);
        if (r != size) throw new IOException("Недостаточно данных для чтения строки");
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }
}