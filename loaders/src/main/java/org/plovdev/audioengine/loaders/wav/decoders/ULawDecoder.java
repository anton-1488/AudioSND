package org.plovdev.audioengine.loaders.wav.decoders;

import java.nio.ByteBuffer;

public class ULawDecoder {
    private static final byte[] ULAW_TO_PCM8_TABLE = {
            0,    0,    1,    1,    2,    2,    3,    3,     // 0x00-0x07
            4,    4,    5,    5,    6,    6,    7,    7,     // 0x08-0x0F
            8,    8,    9,    9,    10,   10,   11,   11,    // 0x10-0x17
            12,   12,   13,   13,   14,   14,   15,   15,    // 0x18-0x1F
            16,   16,   17,   17,   18,   18,   19,   19,    // 0x20-0x27
            20,   20,   21,   21,   22,   22,   23,   23,    // 0x28-0x2F
            24,   24,   25,   25,   26,   26,   27,   27,    // 0x30-0x37
            28,   28,   29,   29,   30,   30,   31,   31,    // 0x38-0x3F
            32,   32,   33,   33,   34,   34,   35,   35,    // 0x40-0x47
            36,   36,   37,   37,   38,   38,   39,   39,    // 0x48-0x4F
            40,   40,   41,   41,   42,   42,   43,   43,    // 0x50-0x57
            44,   44,   45,   45,   46,   46,   47,   47,    // 0x58-0x5F
            48,   48,   49,   49,   50,   50,   51,   51,    // 0x60-0x67
            52,   52,   53,   53,   54,   54,   55,   55,    // 0x68-0x6F
            56,   56,   57,   57,   58,   58,   59,   59,    // 0x70-0x77
            60,   60,   61,   61,   62,   62,   63,   63,    // 0x78-0x7F
            64,   64,   65,   65,   66,   66,   67,   67,    // 0x80-0x87
            68,   68,   69,   69,   70,   70,   71,   71,    // 0x88-0x8F
            72,   72,   73,   73,   74,   74,   75,   75,    // 0x90-0x97
            76,   76,   77,   77,   78,   78,   79,   79,    // 0x98-0x9F
            80,   80,   81,   81,   82,   82,   83,   83,    // 0xA0-0xA7
            84,   84,   85,   85,   86,   86,   87,   87,    // 0xA8-0xAF
            88,   88,   89,   89,   90,   90,   91,   91,    // 0xB0-0xB7
            92,   92,   93,   93,   94,   94,   95,   95,    // 0xB8-0xBF
            96,   96,   97,   97,   98,   98,   99,   99,    // 0xC0-0xC7
            100,  100,  101,  101,  102,  102,  103,  103,   // 0xC8-0xCF
            104,  104,  105,  105,  106,  106,  107,  107,   // 0xD0-0xD7
            108,  108,  109,  109,  110,  110,  111,  111,   // 0xD8-0xDF
            112,  112,  113,  113,  114,  114,  115,  115,   // 0xE0-0xE7
            116,  116,  117,  117,  118,  118,  119,  119,   // 0xE8-0xEF
            120,  120,  121,  121,  122,  122,  123,  123,   // 0xF0-0xF7
            124,  124,  125,  125,  126,  126,  127,  127,   // 0xF8-0xFF
            (byte) 128,  (byte) 128,  (byte) 129, (byte) 129,  (byte) 130,  (byte) 130,  (byte) 131, (byte) 131,   // 0x00-0x07 (повтор для индекса 128-135)
            (byte) 132,  (byte) 132,  (byte) 133, (byte) 133,  (byte) 134,  (byte) 134,  (byte) 135, (byte) 135,   // 0x08-0x0F
            (byte) 136,  (byte) 136,  (byte) 137, (byte) 137,  (byte) 138,  (byte) 138,  (byte) 139, (byte) 139,   // 0x10-0x17
            (byte) 140,  (byte) 140,  (byte) 141, (byte) 141,  (byte) 142,  (byte) 142,  (byte) 143, (byte) 143,   // 0x18-0x1F
            (byte) 144,  (byte) 144,  (byte) 145, (byte) 145,  (byte) 146,  (byte) 146,  (byte) 147, (byte) 147,   // 0x20-0x27
            (byte) 148,  (byte) 148,  (byte) 149, (byte) 149,  (byte) 150,  (byte) 150,  (byte) 151, (byte) 151,   // 0x28-0x2F
            (byte) 152,  (byte) 152,  (byte) 153, (byte) 153,  (byte) 154,  (byte) 154,  (byte) 155, (byte) 155,   // 0x30-0x37
            (byte) 156,  (byte) 156,  (byte) 157, (byte) 157,  (byte) 158,  (byte) 158,  (byte) 159, (byte) 159,   // 0x38-0x3F
            (byte) 160,  (byte) 160,  (byte) 161, (byte) 161,  (byte) 162,  (byte) 162,  (byte) 163, (byte) 163,   // 0x40-0x47
            (byte) 164,  (byte) 164,  (byte) 165, (byte) 165,  (byte) 166,  (byte) 166,  (byte) 167, (byte) 167,   // 0x48-0x4F
            (byte) 168,  (byte) 168,  (byte) 169, (byte) 169,  (byte) 170,  (byte) 170,  (byte) 171, (byte) 171,   // 0x50-0x57
            (byte) 172,  (byte) 172,  (byte) 173, (byte) 173,  (byte) 174,  (byte) 174,  (byte) 175, (byte) 175,   // 0x58-0x5F
            (byte) 176,  (byte) 176,  (byte) 177, (byte) 177,  (byte) 178,  (byte) 178,  (byte) 179, (byte) 179,   // 0x60-0x67
            (byte) 180,  (byte) 180,  (byte) 181, (byte) 181,  (byte) 182,  (byte) 182,  (byte) 183, (byte) 183,   // 0x68-0x6F
            (byte) 184,  (byte) 184,  (byte) 185, (byte) 185,  (byte) 186,  (byte) 186,  (byte) 187, (byte) 187,   // 0x70-0x77
            (byte) 188,  (byte) 188,  (byte) 189, (byte) 189,  (byte) 190,  (byte) 190,  (byte) 191, (byte) 191,   // 0x78-0x7F
            (byte) 192,  (byte) 192,  (byte) 193, (byte) 193,  (byte) 194,  (byte) 194,  (byte) 195, (byte) 195,   // 0x80-0x87
            (byte) 196,  (byte) 196,  (byte) 197, (byte) 197,  (byte) 198,  (byte) 198,  (byte) 199, (byte) 199,   // 0x88-0x8F
            (byte) 200,  (byte) 200,  (byte) 201, (byte) 201,  (byte) 202,  (byte) 202,  (byte) 203, (byte) 203,   // 0x90-0x97
            (byte) 204,  (byte) 204,  (byte) 205, (byte) 205,  (byte) 206,  (byte) 206,  (byte) 207, (byte) 207,   // 0x98-0x9F
            (byte) 208,  (byte) 208,  (byte) 209, (byte) 209,  (byte) 210,  (byte) 210,  (byte) 211, (byte) 211,   // 0xA0-0xA7
            (byte) 212,  (byte) 212,  (byte) 213, (byte) 213,  (byte) 214,  (byte) 214,  (byte) 215, (byte) 215,   // 0xA8-0xAF
            (byte) 216,  (byte) 216,  (byte) 217, (byte) 217,  (byte) 218,  (byte) 218,  (byte) 219, (byte) 219,   // 0xB0-0xB7
            (byte) 220,  (byte) 220,  (byte) 221, (byte) 221,  (byte) 222,  (byte) 222,  (byte) 223, (byte) 223,   // 0xB8-0xBF
            (byte) 224,  (byte) 224,  (byte) 225, (byte) 225,  (byte) 226,  (byte) 226,  (byte) 227, (byte) 227,   // 0xC0-0xC7
            (byte) 228,  (byte) 228,  (byte) 229, (byte) 229,  (byte) 230,  (byte) 230,  (byte) 231, (byte) 231,   // 0xC8-0xCF
            (byte) 232,  (byte) 232,  (byte) 233, (byte) 233,  (byte) 234,  (byte) 234,  (byte) 235, (byte) 235,   // 0xD0-0xD7
            (byte) 236,  (byte) 236,  (byte) 237, (byte) 237,  (byte) 238,  (byte) 238,  (byte) 239, (byte) 239,   // 0xD8-0xDF
            (byte) 240,  (byte) 240,  (byte) 241, (byte) 241,  (byte) 242,  (byte) 242,  (byte) 243, (byte) 243,   // 0xE0-0xE7
            (byte) 244,  (byte) 244,  (byte) 245, (byte) 245,  (byte) 246,  (byte) 246,  (byte) 247, (byte) 247,   // 0xE8-0xEF
            (byte) 248,  (byte) 248,  (byte) 249, (byte) 249,  (byte) 250,  (byte) 250,  (byte) 251, (byte) 251,   // 0xF0-0xF7
            (byte) 252,  (byte) 252,  (byte) 253, (byte) 253,  (byte) 254,  (byte) 254,  (byte) 255, (byte) 255    // 0xF8-0xFF
    };

    public byte decode(byte ulaw) {
        return ULAW_TO_PCM8_TABLE[ulaw & 0xFF];
    }

    public ByteBuffer decode(ByteBuffer ulawData) {
        ulawData.rewind();
        ByteBuffer pcmData = ByteBuffer.allocateDirect(ulawData.capacity());

        for (int i = 0; i < ulawData.capacity(); i++) {
            byte ulaw = ulawData.get();
            byte pcm = decode(ulaw);
            pcmData.put(pcm);
        }

        pcmData.rewind();
        return pcmData;
    }
}