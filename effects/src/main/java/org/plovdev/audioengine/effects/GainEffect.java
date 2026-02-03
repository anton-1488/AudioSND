package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GainEffect implements AudioEffect {

    private TrackFormat format;
    private ByteOrder order;
    private float gain;

    public GainEffect(float gain) {
        this.gain = gain;
    }

    @Override
    public void setup(TrackFormat format) {
        this.format = format;
        this.order  = format.byteOrder();

        switch (format.audioCodec()) {
            case PCM8, PCM16, PCM24, PCM32, FLOAT32, FLOAT64 -> {}
            default -> throw new IllegalArgumentException(
                    "GainEffect не поддерживает формат: " + format.audioCodec()
            );
        }
    }

    @Override
    public ByteBuffer process(ByteBuffer buffer) {
        buffer.order(order);

        switch (format.audioCodec()) {
            case PCM8    -> processPCM8(buffer);
            case PCM16   -> processPCM16(buffer);
            case PCM24   -> processPCM24(buffer);
            case PCM32   -> processPCM32(buffer);
            case FLOAT32 -> processFloat32(buffer);
            case FLOAT64 -> processFloat64(buffer);
        }

        return buffer;
    }

    /* ================= PCM ================= */

    private void processPCM8(ByteBuffer b) {
        for (int i = 0; i < b.remaining(); i++) {
            int v = (int) (b.get(i) * gain);
            if (v > 127) v = 127;
            else if (v < -128) v = -128;
            b.put(i, (byte) v);
        }
    }

    private void processPCM16(ByteBuffer b) {
        for (int i = 0; i < b.remaining(); i += 2) {
            int v = (int) (b.getShort(i) * gain);
            if (v > 32767) v = 32767;
            else if (v < -32768) v = -32768;
            b.putShort(i, (short) v);
        }
    }

    private void processPCM24(ByteBuffer b) {
        for (int i = 0; i < b.remaining(); i += 3) {
            int sample =
                    (b.get(i) & 0xFF) |
                            ((b.get(i + 1) & 0xFF) << 8) |
                            (b.get(i + 2) << 16);

            sample = (sample << 8) >> 8; // sign extend

            int v = (int) (sample * gain);
            if (v > 8_388_607) v = 8_388_607;
            else if (v < -8_388_608) v = -8_388_608;

            b.put(i,     (byte) v);
            b.put(i + 1, (byte) (v >> 8));
            b.put(i + 2, (byte) (v >> 16));
        }
    }

    private void processPCM32(ByteBuffer b) {
        for (int i = 0; i < b.remaining(); i += 4) {
            long v = (long) (b.getInt(i) * gain);
            if (v > Integer.MAX_VALUE) v = Integer.MAX_VALUE;
            else if (v < Integer.MIN_VALUE) v = Integer.MIN_VALUE;
            b.putInt(i, (int) v);
        }
    }

    /* ================= FLOAT ================= */

    private void processFloat32(ByteBuffer b) {
        for (int i = 0; i < b.remaining(); i += 4) {
            b.putFloat(i, b.getFloat(i) * gain);
        }
    }

    private void processFloat64(ByteBuffer b) {
        for (int i = 0; i < b.remaining(); i += 8) {
            b.putDouble(i, b.getDouble(i) * gain);
        }
    }

    /* ================= API ================= */

    public void setGainLinear(float gain) {
        this.gain = Math.max(0.0f, gain);
    }

    public void setGainDb(float db) {
        this.gain = (float) Math.pow(10.0, db / 20.0);
    }

    public float getGainLinear() {
        return gain;
    }

    public float getGainDb() {
        return (float) (20.0 * Math.log10(gain));
    }
}