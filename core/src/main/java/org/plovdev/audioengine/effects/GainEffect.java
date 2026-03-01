package org.plovdev.audioengine.effects;

import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GainEffect implements AudioEffect {

    private TrackFormat format;
    private float gain = 0.5f;

    public GainEffect(float gain) {
        if (gain < -20 || gain > 20) {
            throw new IllegalArgumentException("Gain value is incorrect");
        }
        this.gain = gain;
    }

    public GainEffect() {
    }

    @Override
    public void setup(TrackFormat format) {
        this.format = format;
    }

    @Override
    public ByteBuffer process(ByteBuffer inputBuffer) {
        if (format == null) {
            throw new IllegalStateException("GainEffect не инициализирован");
        }

        int originalPosition = inputBuffer.position();
        int originalLimit = inputBuffer.limit();

        try {
            inputBuffer.rewind();

            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(inputBuffer.capacity());
            outputBuffer.order(format.byteOrder());
            applyGain(inputBuffer, outputBuffer);

            outputBuffer.flip();
            inputBuffer.position(originalPosition);
            inputBuffer.limit(originalLimit);

            return outputBuffer;
        } catch (Exception e) {
            inputBuffer.position(originalPosition);
            inputBuffer.limit(originalLimit);
            throw e;
        }
    }

    private void applyGain(ByteBuffer input, ByteBuffer output) {
        ByteOrder originalInputOrder = input.order();
        input.order(format.byteOrder());

        try {
            switch (format.audioCodec()) {
                case PCM16 -> applyGainPCM16(input, output);
                case PCM8 -> applyGainPCM8(input, output);
                case PCM24 -> applyGainPCM24(input, output);
                case PCM32 -> applyGainPCM32(input, output);
                case FLOAT32 -> applyGainFloat32(input, output);
                case FLOAT64 -> applyGainFloat64(input, output);
                default -> throw new IllegalArgumentException("Неподдерживаемый формат: " + format.audioCodec());
            }
        } finally {
            input.order(originalInputOrder);
        }
    }

    private void applyGainPCM16(ByteBuffer input, ByteBuffer output) {
        int samples = input.capacity() / 2;

        for (int i = 0; i < samples; i++) {
            short sample = input.getShort();

            // 1. Конвертируем в float [-1.0, 1.0]
            float normalized = sample / 32768.0f;

            // 2. Применяем усиление
            float amplified = normalized * gain;
            amplified = fastTanh(amplified);

            // 4. Конвертируем обратно в short
            short result = (short) (amplified * 32767.0f);

            output.putShort(result);
        }
    }

    private void applyGainPCM8(ByteBuffer input, ByteBuffer output) {
        int samples = input.capacity(); // 1 байт = 1 sample

        for (int i = 0; i < samples; i++) {
            byte sample = input.get();

            float normalized = sample / 128.0f;

            // Применяем gain и tanh
            float processed = normalized * gain;
            processed = fastTanh(processed);

            // Обратно в byte [-128..127]
            int result = Math.round(processed * 127.0f);
            if (result > 127) result = 127;
            else if (result < -128) result = -128;

            output.put((byte) result);
        }
    }

    private void applyGainPCM24(ByteBuffer input, ByteBuffer output) {
        int samples = input.capacity() / 3;

        for (int i = 0; i < samples; i++) {
            int b0 = input.get() & 0xFF;
            int b1 = input.get() & 0xFF;
            int b2 = input.get() & 0xFF;

            int sample = b0 | (b1 << 8) | (b2 << 16);
            if ((sample & 0x800000) != 0) {
                sample |= 0xFF000000;
            }

            // Конвертируем в float [-1.0, 1.0]
            float normalized = sample / 8388608.0f;

            // Применяем gain и tanh
            float processed = normalized * gain;
            processed = fastTanh(processed);

            // Обратно в 24-bit
            int result = Math.round(processed * 8388607.0f);
            if (result > 8388607) result = 8388607;
            else if (result < -8388608) result = -8388608;

            // Записываем 3 байта
            output.put((byte) (result & 0xFF));
            output.put((byte) ((result >> 8) & 0xFF));
            output.put((byte) ((result >> 16) & 0xFF));
        }
    }

    private void applyGainPCM32(ByteBuffer input, ByteBuffer output) {
        int samples = input.capacity() / 4;

        for (int i = 0; i < samples; i++) {
            int sample = input.getInt();
            double normalized = sample / 2147483648.0;
            double processed = normalized * gain;
            processed = Math.tanh(processed);
            double denormalized = processed * 2147483647.0;
            long rounded = Math.round(denormalized);
            int result = (int) rounded;

            output.putInt(result);
        }
    }

    private void applyGainFloat32(ByteBuffer input, ByteBuffer output) {
        int bytesToProcess = input.capacity();
        int samples = bytesToProcess / 4;

        for (int i = 0; i < samples; i++) {
            float sample = input.getFloat();
            float result = sample * gain;

            result = fastTanh(result);

            output.putFloat(result);
        }
    }

    private void applyGainFloat64(ByteBuffer input, ByteBuffer output) {
        int bytesToProcess = input.capacity();
        int samples = bytesToProcess / 8;

        for (int i = 0; i < samples; i++) {
            double sample = input.getDouble();
            double result = sample * gain;

            result = Math.tanh(result);

            output.putDouble(result);
        }
    }

    private float fastTanh(float x) {
        float x2 = x * x;
        return x * (27 + x2) / (27 + 9 * x2);
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        if (gain < -10 || gain > 10) {
            throw new IllegalArgumentException("Gain value is incorrect");
        }
        this.gain = gain;
    }
}