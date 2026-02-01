package org.plovdev.audioengine.loaders.wav.read.parsers;

import org.plovdev.audioengine.loaders.wav.chunks.ListChunk;
import org.plovdev.audioengine.loaders.wav.chunks.TagEntry;
import org.plovdev.audioengine.loaders.wav.read.WavChunkParser;
import org.plovdev.audioengine.loaders.wav.struct.Chunk;
import org.plovdev.audioengine.loaders.wav.struct.WavChunkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ID3ChunkParser implements WavChunkParser {
    private static final Logger log = LoggerFactory.getLogger(ID3ChunkParser.class);

    @Override
    public boolean canParse(WavChunkId id) {
        return id == WavChunkId.ID3_HEAD;
    }

    @Override
    public Chunk parse(byte[] body) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(body);
             DataInputStream input = new DataInputStream(bis)) {

            // 1. Header (10 bytes)
            byte[] header = new byte[3];
            input.readFully(header);
            if (!"ID3".equals(new String(header, StandardCharsets.ISO_8859_1))) {
                throw new IllegalArgumentException("Not ID3 signature");
            }

            // 2. Version (2 bytes)
            int majorVersion = input.readUnsignedByte(); // 3 для ID3v2.3
            int minorVersion = input.readUnsignedByte(); // 0 для ID3v2.3.0

            // 3. Flags (1 byte)
            int flags = input.readUnsignedByte();
            boolean unsynchronisation = (flags & 0x80) != 0;
            boolean extendedHeader = (flags & 0x40) != 0;
            boolean experimental = (flags & 0x20) != 0;

            // 4. Size (4 bytes, synchsafe)
            int size = readSynchsafeInt(input);

            // 5. Skip extended header if present
            if (extendedHeader) {
                int extHeaderSize = readSynchsafeInt(input);
                input.skipBytes(extHeaderSize - 4);
            }

            // 6. Parse frames
            List<Chunk> chunks = new ArrayList<>();
            int bytesRead = 0;

            while (bytesRead < size && input.available() >= 10) {
                TagEntry entry = parseFrame(input, majorVersion);
                if (entry != null) {
                    chunks.add(entry);
                    bytesRead += entry.getSize() + (majorVersion == 3 ? 10 : 12);
                }
            }

            return new ListChunk(WavChunkId.ID3_HEAD, chunks);

        } catch (Exception e) {
            log.error("Error parsing ID3 chunk", e);
            return new Chunk(WavChunkId.UNKNOWN, body.length, body);
        }
    }

    private int readSynchsafeInt(DataInputStream input) throws IOException {
        byte[] bytes = new byte[4];
        input.readFully(bytes);
        return ((bytes[0] & 0x7F) << 21) |
                ((bytes[1] & 0x7F) << 14) |
                ((bytes[2] & 0x7F) << 7) |
                (bytes[3] & 0x7F);
    }

    private TagEntry parseFrame(DataInputStream input, int version) throws IOException {
        // Read frame header
        byte[] frameIdBytes = new byte[4];
        input.readFully(frameIdBytes);
        String frameId = new String(frameIdBytes, StandardCharsets.ISO_8859_1);

        if (frameId.charAt(0) == '\0') {
            return null;
        }

        // Read size
        int frameSize;
        if (version == 3) {
            frameSize = input.readInt();
        } else {
            frameSize = readSynchsafeInt(input);
        }

        // Read flags (2 bytes)
        int frameFlags = input.readUnsignedShort();

        // Read frame data
        byte[] data = new byte[frameSize];
        input.readFully(data);

        // Parse based on frame type
        String content = parseFrameContent(frameId, data);

        WavChunkId tagId = WavChunkId.fromString(frameId);
        if (tagId == null) {
            tagId = WavChunkId.UNKNOWN;
        }

        return new TagEntry(tagId, frameSize, data, content);
    }

    private String parseFrameContent(String frameId, byte[] data) {
        if (data.length == 0) return "";

        // First byte is text encoding
        int encoding = data[0] & 0xFF;
        String charset = switch (encoding) {
            case 1 -> "UTF-16";
            case 2 -> "UTF-16BE";
            case 3 -> "UTF-8";
            default -> "ISO-8859-1";
        };

        try {
            return new String(data, 1, data.length - 1, charset).trim().replace("\u0000", "");
        } catch (Exception e) {
            return new String(data, 1, data.length - 1, StandardCharsets.ISO_8859_1).trim().replace("\u0000", "");
        }
    }
}