package org.plovdev.audioengine.loaders.wav.read.parsers;

import org.plovdev.audioengine.loaders.ExportUtils;
import org.plovdev.audioengine.loaders.wav.chunks.ListChunk;
import org.plovdev.audioengine.loaders.wav.chunks.TagEntry;
import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ListChunkParser implements WavChunkParser {
    private static final Logger log = LoggerFactory.getLogger(ListChunkParser.class);

    @Override
    public boolean canParse(WavChunkId id) {
        return id == WavChunkId.LIST;
    }

    @Override
    public Chunk parse(byte[] body) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(body));
            byte[] typeBytes = new byte[4];
            if (inputStream.read(typeBytes) != 4) {
                log.warn("Readed typeBytes size is non equal excepted size!");
            }
            String chunkTypeStr = new String(typeBytes, StandardCharsets.US_ASCII);
            WavChunkId typeId = WavChunkId.fromString(chunkTypeStr);

            if (typeId == null) {
                typeId = WavChunkId.UNKNOWN;
            }
            List<Chunk> chunks = new ArrayList<>();
            while (inputStream.available() >= 8) {
                chunks.add(parseEntry(inputStream));
            }
            return new ListChunk(typeId, chunks);
        } catch (Exception e) {
            log.error("Error to parse chunk: ", e);
        }
        return new Chunk(WavChunkId.UNKNOWN, body.length, body);
    }

    private TagEntry parseEntry(BufferedInputStream inputStream) throws IOException {
        byte[] tag = new byte[4];
        if (inputStream.read(tag) != 4) {
            log.warn("Readed 'tag' size is non equal excepted size!");
        }

        String tagName = new String(tag, StandardCharsets.US_ASCII);
        WavChunkId tagId = WavChunkId.fromString(tagName);
        if (tagId == null) {
            tagId = WavChunkId.UNKNOWN;
        }

        byte[] sizeBytes = new byte[4];
        if (inputStream.read(sizeBytes) != 4) {
            log.warn("Readed 'sizeBytes' size is non equal excepted size!");
        }
        int size = ExportUtils.bytesToInt(sizeBytes, 0, 4);
        if (size % 2 != 0) {
            size++;
        }

        byte[] content = new byte[size];
        if (inputStream.read(content) != size) {
            log.warn("Readed content size is non equal excepted size!");
        }

        String contentStr = new String(content, StandardCharsets.US_ASCII).replace("\u0000", "");
        return new TagEntry(tagId, size, content, contentStr);
    }
}
