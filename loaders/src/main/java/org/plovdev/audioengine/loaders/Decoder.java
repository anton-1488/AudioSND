package org.plovdev.audioengine.loaders;

import java.nio.ByteBuffer;

public interface Decoder {
    ByteBuffer decode(ByteBuffer input);
}