#include <AudioToolbox/AudioToolbox.h>
#include <AudioUnit/AudioUnit.h>
#include <jni.h>

#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <iostream>

#include <string>
#include <algorithm>
#include <cmath>

#include "org_plovdev_audioengine_devices_NativeOutputAudioDevice.h"

// ======================================================
// RingBuffer (lock-free, frames-based, float32)
// ======================================================

static JavaVM* javaVM = nullptr;
static jobject nativeOutputDeivce = nullptr;

struct RingBuffer {
    float* data = nullptr;          // interleaved float samples
    uint32_t frames = 0;            // total frames
    uint32_t channels = 0;

    std::atomic<uint32_t> read{0};  // frame index
    std::atomic<uint32_t> write{0}; // frame index
};

static inline uint32_t rbAvailable(const RingBuffer& rb) {
    uint32_t write = rb.write.load(std::memory_order_acquire);
    uint32_t read = rb.read.load(std::memory_order_acquire);

    if (write >= read) {
        return write - read;
    } else {
        return rb.frames - read + write;
    }
}

static inline uint32_t rbFree(const RingBuffer& rb) {
    return rb.frames - rbAvailable(rb) - 1;
}

typedef void (*ConverterFunction)(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale);

// ======================================================
// AudioContext
// ======================================================

struct AudioContext {
    AudioUnit unit = nullptr;

    RingBuffer rb;

    uint32_t sampleRate = 0;
    uint32_t channels = 0;

    // Информация о формате входных данных
    uint32_t bitsPerSample = 16;      // по умолчанию 16-bit
    bool isFloatFormat = false;       // float или integer
    bool isSigned = true;             // signed или unsigned
    bool isBigEndian = false;         // порядок байт
    ConverterFunction converter = nullptr;
    float scale = 1.0f;

    uint32_t bytesPerSample = 3;

    std::atomic<bool> running{false};
    std::atomic<uint64_t> underruns{0};
    uint32_t bytesPerFrame = 0;
};

static AudioContext* ctx = nullptr;


// ======================================================
// Вспомогательные функции конвертации
// ======================================================

// ==================== 16-bit Signed Little-Endian ====================
static void convertPCM16SLE_Batch(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale) {
    const int16_t* src16 = reinterpret_cast<const int16_t*>(src);
    const uint32_t totalSamples = frames * channels;

    // Развернутый цикл для лучшей оптимизации
    for (uint32_t i = 0; i + 7 < totalSamples; i += 8) {
        dst[i]   = src16[i]   * scale;
        dst[i+1] = src16[i+1] * scale;
        dst[i+2] = src16[i+2] * scale;
        dst[i+3] = src16[i+3] * scale;
        dst[i+4] = src16[i+4] * scale;
        dst[i+5] = src16[i+5] * scale;
        dst[i+6] = src16[i+6] * scale;
        dst[i+7] = src16[i+7] * scale;
    }

    // Обрабатываем остаток
    for (uint32_t i = totalSamples & ~7; i < totalSamples; ++i) {
        dst[i] = src16[i] * scale;
    }
}

// ==================== 16-bit Signed Big-Endian ====================
static void convertPCM16SBE_Batch(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale) {
    const uint32_t totalSamples = frames * channels;

    for (uint32_t i = 0; i < totalSamples; ++i) {
        const uint8_t* samplePtr = &src[i * 2];
        int16_t sample = (samplePtr[0] << 8) | samplePtr[1];
        dst[i] = sample * scale;
    }
}

// ==================== 24-bit Signed Little-Endian ====================
static void convertPCM24SLE_Batch(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale) {
    const uint32_t totalSamples = frames * channels;

    for (uint32_t i = 0; i < totalSamples; ++i) {
        const uint8_t* samplePtr = &src[i * 3];

        // Эффективное чтение 24-bit LE
        int32_t sample = (samplePtr[0] << 8) |
                        (samplePtr[1] << 16) |
                        (samplePtr[2] << 24);
        sample >>= 8; // Арифметический сдвиг для знакового расширения

        dst[i] = sample * scale;
    }
}

// ==================== 24-bit Signed Big-Endian ====================
static void convertPCM24SBE_Batch(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale) {
    const uint32_t totalSamples = frames * channels;

    for (uint32_t i = 0; i < totalSamples; ++i) {
        const uint8_t* samplePtr = &src[i * 3];

        // 24-bit BE
        int32_t sample = (samplePtr[0] << 24) |
                        (samplePtr[1] << 16) |
                        (samplePtr[2] << 8);
        sample >>= 8; // Арифметический сдвиг

        dst[i] = sample * scale;
    }
}

// ==================== 8-bit Unsigned ====================
static void convertPCM8U_Batch(const uint8_t* src, float* dst,
                              uint32_t frames, uint32_t channels,
                              float scale) {
    const uint32_t totalSamples = frames * channels;
    const float offset = -128.0f;

    for (uint32_t i = 0; i < totalSamples; ++i) {
        dst[i] = (src[i] + offset) * scale;
    }
}

// ==================== 8-bit Signed ====================
static void convertPCM8S_Batch(const uint8_t* src, float* dst,
                              uint32_t frames, uint32_t channels,
                              float scale) {
    const uint32_t totalSamples = frames * channels;
    const int8_t* src8 = reinterpret_cast<const int8_t*>(src);

    for (uint32_t i = 0; i < totalSamples; ++i) {
        dst[i] = src8[i] * scale;
    }
}

// ==================== Float 32 Little-Endian ====================
static void convertFloat32LE_Batch(const uint8_t* src, float* dst,
                                  uint32_t frames, uint32_t channels,
                                  float scale) {
    // Простое копирование (формат уже float)
    const uint32_t totalBytes = frames * channels * sizeof(float);
    memcpy(dst, src, totalBytes);
}

// ==================== Float 32 Big-Endian ====================
static void convertFloat32BE_Batch(const uint8_t* src, float* dst,
                                  uint32_t frames, uint32_t channels,
                                  float scale) {
    const uint32_t totalSamples = frames * channels;

    for (uint32_t i = 0; i < totalSamples; ++i) {
        const uint8_t* samplePtr = &src[i * 4];
        union {
            uint32_t i;
            float f;
        } u;
        u.i = (samplePtr[0] << 24) | (samplePtr[1] << 16) |
              (samplePtr[2] << 8) | samplePtr[3];
        dst[i] = u.f;
    }
}

// ==================== Float 64 Little-Endian ====================
static void convertFloat64LE_Batch(const uint8_t* src, float* dst,
                                  uint32_t frames, uint32_t channels,
                                  float scale) {
    const uint32_t totalSamples = frames * channels;

    // Обрабатываем блоками по 4 сэмпла для лучшей кэш-локальности
    for (uint32_t i = 0; i + 3 < totalSamples; i += 4) {
        // Сэмпл 0
        const uint8_t* ptr0 = &src[i * 8];
        union { uint64_t i; double d; } u0;
        u0.i = (static_cast<uint64_t>(ptr0[7]) << 56) |
               (static_cast<uint64_t>(ptr0[6]) << 48) |
               (static_cast<uint64_t>(ptr0[5]) << 40) |
               (static_cast<uint64_t>(ptr0[4]) << 32) |
               (static_cast<uint64_t>(ptr0[3]) << 24) |
               (static_cast<uint64_t>(ptr0[2]) << 16) |
               (static_cast<uint64_t>(ptr0[1]) << 8) |
               static_cast<uint64_t>(ptr0[0]);

        // Сэмпл 1
        const uint8_t* ptr1 = &src[(i+1) * 8];
        union { uint64_t i; double d; } u1;
        u1.i = (static_cast<uint64_t>(ptr1[7]) << 56) |
               (static_cast<uint64_t>(ptr1[6]) << 48) |
               (static_cast<uint64_t>(ptr1[5]) << 40) |
               (static_cast<uint64_t>(ptr1[4]) << 32) |
               (static_cast<uint64_t>(ptr1[3]) << 24) |
               (static_cast<uint64_t>(ptr1[2]) << 16) |
               (static_cast<uint64_t>(ptr1[1]) << 8) |
               static_cast<uint64_t>(ptr1[0]);

        // Сэмпл 2
        const uint8_t* ptr2 = &src[(i+2) * 8];
        union { uint64_t i; double d; } u2;
        u2.i = (static_cast<uint64_t>(ptr2[7]) << 56) |
               (static_cast<uint64_t>(ptr2[6]) << 48) |
               (static_cast<uint64_t>(ptr2[5]) << 40) |
               (static_cast<uint64_t>(ptr2[4]) << 32) |
               (static_cast<uint64_t>(ptr2[3]) << 24) |
               (static_cast<uint64_t>(ptr2[2]) << 16) |
               (static_cast<uint64_t>(ptr2[1]) << 8) |
               static_cast<uint64_t>(ptr2[0]);

        // Сэмпл 3
        const uint8_t* ptr3 = &src[(i+3) * 8];
        union { uint64_t i; double d; } u3;
        u3.i = (static_cast<uint64_t>(ptr3[7]) << 56) |
               (static_cast<uint64_t>(ptr3[6]) << 48) |
               (static_cast<uint64_t>(ptr3[5]) << 40) |
               (static_cast<uint64_t>(ptr3[4]) << 32) |
               (static_cast<uint64_t>(ptr3[3]) << 24) |
               (static_cast<uint64_t>(ptr3[2]) << 16) |
               (static_cast<uint64_t>(ptr3[1]) << 8) |
               static_cast<uint64_t>(ptr3[0]);

        dst[i]   = static_cast<float>(u0.d);
        dst[i+1] = static_cast<float>(u1.d);
        dst[i+2] = static_cast<float>(u2.d);
        dst[i+3] = static_cast<float>(u3.d);
    }

    // Остаток
    for (uint32_t i = totalSamples & ~3; i < totalSamples; ++i) {
        const uint8_t* ptr = &src[i * 8];
        union { uint64_t i; double d; } u;
        u.i = (static_cast<uint64_t>(ptr[7]) << 56) |
              (static_cast<uint64_t>(ptr[6]) << 48) |
              (static_cast<uint64_t>(ptr[5]) << 40) |
              (static_cast<uint64_t>(ptr[4]) << 32) |
              (static_cast<uint64_t>(ptr[3]) << 24) |
              (static_cast<uint64_t>(ptr[2]) << 16) |
              (static_cast<uint64_t>(ptr[1]) << 8) |
              static_cast<uint64_t>(ptr[0]);
        dst[i] = static_cast<float>(u.d);
    }
}

// ==================== Float 64 Big-Endian ====================
static void convertFloat64BE_Batch(const uint8_t* src, float* dst,
                                  uint32_t frames, uint32_t channels,
                                  float scale) {
    const uint32_t totalSamples = frames * channels;

    for (uint32_t i = 0; i + 3 < totalSamples; i += 4) {
        // Сэмпл 0
        const uint8_t* ptr0 = &src[i * 8];
        union { uint64_t i; double d; } u0;
        u0.i = (static_cast<uint64_t>(ptr0[0]) << 56) |
               (static_cast<uint64_t>(ptr0[1]) << 48) |
               (static_cast<uint64_t>(ptr0[2]) << 40) |
               (static_cast<uint64_t>(ptr0[3]) << 32) |
               (static_cast<uint64_t>(ptr0[4]) << 24) |
               (static_cast<uint64_t>(ptr0[5]) << 16) |
               (static_cast<uint64_t>(ptr0[6]) << 8) |
               static_cast<uint64_t>(ptr0[7]);

        // Сэмпл 1
        const uint8_t* ptr1 = &src[(i+1) * 8];
        union { uint64_t i; double d; } u1;
        u1.i = (static_cast<uint64_t>(ptr1[0]) << 56) |
               (static_cast<uint64_t>(ptr1[1]) << 48) |
               (static_cast<uint64_t>(ptr1[2]) << 40) |
               (static_cast<uint64_t>(ptr1[3]) << 32) |
               (static_cast<uint64_t>(ptr1[4]) << 24) |
               (static_cast<uint64_t>(ptr1[5]) << 16) |
               (static_cast<uint64_t>(ptr1[6]) << 8) |
               static_cast<uint64_t>(ptr1[7]);

        // Сэмпл 2
        const uint8_t* ptr2 = &src[(i+2) * 8];
        union { uint64_t i; double d; } u2;
        u2.i = (static_cast<uint64_t>(ptr2[0]) << 56) |
               (static_cast<uint64_t>(ptr2[1]) << 48) |
               (static_cast<uint64_t>(ptr2[2]) << 40) |
               (static_cast<uint64_t>(ptr2[3]) << 32) |
               (static_cast<uint64_t>(ptr2[4]) << 24) |
               (static_cast<uint64_t>(ptr2[5]) << 16) |
               (static_cast<uint64_t>(ptr2[6]) << 8) |
               static_cast<uint64_t>(ptr2[7]);

        // Сэмпл 3
        const uint8_t* ptr3 = &src[(i+3) * 8];
        union { uint64_t i; double d; } u3;
        u3.i = (static_cast<uint64_t>(ptr3[0]) << 56) |
               (static_cast<uint64_t>(ptr3[1]) << 48) |
               (static_cast<uint64_t>(ptr3[2]) << 40) |
               (static_cast<uint64_t>(ptr3[3]) << 32) |
               (static_cast<uint64_t>(ptr3[4]) << 24) |
               (static_cast<uint64_t>(ptr3[5]) << 16) |
               (static_cast<uint64_t>(ptr3[6]) << 8) |
               static_cast<uint64_t>(ptr3[7]);

        dst[i]   = static_cast<float>(u0.d);
        dst[i+1] = static_cast<float>(u1.d);
        dst[i+2] = static_cast<float>(u2.d);
        dst[i+3] = static_cast<float>(u3.d);
    }

    // Остаток
    for (uint32_t i = totalSamples & ~3; i < totalSamples; ++i) {
        const uint8_t* ptr = &src[i * 8];
        union { uint64_t i; double d; } u;
        u.i = (static_cast<uint64_t>(ptr[0]) << 56) |
              (static_cast<uint64_t>(ptr[1]) << 48) |
              (static_cast<uint64_t>(ptr[2]) << 40) |
              (static_cast<uint64_t>(ptr[3]) << 32) |
              (static_cast<uint64_t>(ptr[4]) << 24) |
              (static_cast<uint64_t>(ptr[5]) << 16) |
              (static_cast<uint64_t>(ptr[6]) << 8) |
              static_cast<uint64_t>(ptr[7]);
        dst[i] = static_cast<float>(u.d);
    }
}

// ==================== 16-bit Unsigned Little-Endian ====================
static void convertPCM16ULE_Batch(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale) {
    const uint32_t totalSamples = frames * channels;
    const float offset = -32768.0f; // Для преобразования unsigned → signed

    for (uint32_t i = 0; i + 3 < totalSamples; i += 4) {
        // Читаем как uint16_t и преобразуем
        const uint16_t* src16 = reinterpret_cast<const uint16_t*>(src);

        dst[i]   = (static_cast<int32_t>(src16[i]) + offset) * scale;
        dst[i+1] = (static_cast<int32_t>(src16[i+1]) + offset) * scale;
        dst[i+2] = (static_cast<int32_t>(src16[i+2]) + offset) * scale;
        dst[i+3] = (static_cast<int32_t>(src16[i+3]) + offset) * scale;
    }

    // Остаток
    const uint16_t* src16 = reinterpret_cast<const uint16_t*>(src);
    for (uint32_t i = totalSamples & ~3; i < totalSamples; ++i) {
        dst[i] = (static_cast<int32_t>(src16[i]) + offset) * scale;
    }
}

// ==================== 32-bit Signed Little-Endian ====================
static void convertPCM32SLE_Batch(const uint8_t* src, float* dst,
                                 uint32_t frames, uint32_t channels,
                                 float scale) {
    const int32_t* src32 = reinterpret_cast<const int32_t*>(src);
    const uint32_t totalSamples = frames * channels;

    // Развернутый цикл
    for (uint32_t i = 0; i + 7 < totalSamples; i += 8) {
        dst[i]   = src32[i]   * scale;
        dst[i+1] = src32[i+1] * scale;
        dst[i+2] = src32[i+2] * scale;
        dst[i+3] = src32[i+3] * scale;
        dst[i+4] = src32[i+4] * scale;
        dst[i+5] = src32[i+5] * scale;
        dst[i+6] = src32[i+6] * scale;
        dst[i+7] = src32[i+7] * scale;
    }

    // Остаток
    for (uint32_t i = totalSamples & ~7; i < totalSamples; ++i) {
        dst[i] = src32[i] * scale;
    }
}



static void initConverter(AudioContext* ctx) {
    // Выбираем scale в зависимости от формата
    if (ctx->isFloatFormat) {
        // Для float форматов scale = 1.0f
        ctx->scale = 1.0f;

        if (ctx->bitsPerSample == 32) {
            if (ctx->isBigEndian) {
                ctx->converter = convertFloat32BE_Batch;
            } else {
                ctx->converter = convertFloat32LE_Batch;
            }
        } else if (ctx->bitsPerSample == 64) {
            if (ctx->isBigEndian) {
                ctx->converter = convertFloat64BE_Batch;
            } else {
                ctx->converter = convertFloat64LE_Batch;
            }
        }
    } else {
        // Для PCM форматов вычисляем scale
        if (ctx->isSigned) {
            // Для signed: делим на 2^(bits-1)
            switch (ctx->bitsPerSample) {
                case 8:  ctx->scale = 1.0f / 128.0f; break;
                case 16: ctx->scale = 1.0f / 32768.0f; break;
                case 24: ctx->scale = 1.0f / 8388608.0f; break;
                case 32: ctx->scale = 1.0f / 2147483648.0f; break;
                default: ctx->scale = 1.0f; break;
            }
        } else {
            // Для unsigned: смещаем на 2^(bits-1)
            switch (ctx->bitsPerSample) {
                case 8:  ctx->scale = 1.0f / 127.0f; break; // (255-128)/127
                default: ctx->scale = 1.0f; break;
            }
        }

        // Выбираем оптимальный конвертер по формату
        uint32_t formatKey = (ctx->bitsPerSample << 8) |
                            (ctx->isBigEndian ? 0x01 : 0x00) |
                            (ctx->isSigned ? 0x02 : 0x00);

        switch (formatKey) {
            // 16-bit signed little-endian (наиболее частый)
            case 0x1002: // bits=16, signed, little-endian
                ctx->converter = convertPCM16SLE_Batch;
                break;

            // 16-bit signed big-endian
            case 0x1003: // bits=16, signed, big-endian
                ctx->converter = convertPCM16SBE_Batch;
                break;

            // 16-bit unsigned little-endian
            case 0x1000: // bits=16, unsigned, little-endian
                ctx->converter = convertPCM16ULE_Batch;
                break;

            // 24-bit signed little-endian
            case 0x1802: // bits=24, signed, little-endian
                ctx->converter = convertPCM24SLE_Batch;
                break;

            // 24-bit signed big-endian
            case 0x1803: // bits=24, signed, big-endian
                ctx->converter = convertPCM24SBE_Batch;
                break;

            // 32-bit signed little-endian
            case 0x2002: // bits=32, signed, little-endian
                ctx->converter = convertPCM32SLE_Batch;
                break;

            // 8-bit unsigned
            case 0x0800: // bits=8, unsigned
                ctx->converter = convertPCM8U_Batch;
                break;

            // 8-bit signed
            case 0x0802: // bits=8, signed
                ctx->converter = convertPCM8S_Batch;
                break;
        }
    }

    // Предвычисляем bytesPerFrame
    ctx->bytesPerFrame = ctx->bytesPerSample * ctx->channels;

    printf("Selected converter: %p, scale: %f\n", ctx->converter, ctx->scale);
}

// ======================================================
// CoreAudio render callback (REALTIME SAFE)
// ======================================================

static void requestChunkProviderMethod(jint requestBytes) {
    JNIEnv* env = nullptr;
    javaVM->AttachCurrentThread((void**)&env, nullptr);

    jclass devCls = env->FindClass("Lorg/plovdev/audioengine/devices/NativeOutputAudioDevice;");
    if (!devCls) return;

    jfieldID fid = env->GetFieldID(devCls, "provider", "Lorg/plovdev/audioengine/devices/ChunkProvider;");
    if (!fid) return;

    jobject providerObj = env->GetObjectField(nativeOutputDeivce, fid);
    if (!providerObj) return;

    // Получаем класс provider
    jclass providerCls = env->GetObjectClass(providerObj);
    if (!providerCls) return;

    // Метод void onNextChunkRequired(int)
    jmethodID mid = env->GetMethodID(providerCls, "onNextChunkRequired", "(I)V");
    if (!mid) return;

    // Вызываем метод на объекте provider
    env->CallVoidMethod(providerObj, mid, requestBytes);
}

static OSStatus audioRenderCallback(
        void* refCon,
        AudioUnitRenderActionFlags*,
        const AudioTimeStamp*,
        UInt32,
        UInt32 inNumberFrames,
        AudioBufferList* ioData) {

    auto* c = static_cast<AudioContext*>(refCon);
    float* out = static_cast<float*>(ioData->mBuffers[0].mData);

    const uint32_t channels = c->channels;
    const uint32_t samplesRequested = inNumberFrames * channels;

    if (!c || !c->running.load(std::memory_order_relaxed)) {
        memset(out, 0, samplesRequested * sizeof(float));
        return noErr;
    }

    RingBuffer& rb = c->rb;
    uint32_t availableFrames = rbAvailable(rb);
    if (availableFrames < 512) {
        //jint requestBytes = (inNumberFrames - availableFrames) * c->channels * sizeof(float);
        //requestChunkProviderMethod(requestBytes);
        memset(out, 0, samplesRequested * sizeof(float));
        c->underruns.fetch_add(1, std::memory_order_relaxed);
        return noErr; // сразу выходим
    }

    uint32_t readFrame = rb.read.load(std::memory_order_relaxed);
    uint32_t firstFrames = rb.frames - readFrame;

    if (firstFrames >= inNumberFrames) {
        memcpy(out,
               rb.data + readFrame * channels,
               samplesRequested * sizeof(float));
    } else {
        uint32_t firstSamples = firstFrames * channels;
        uint32_t secondSamples = (inNumberFrames - firstFrames) * channels;

        memcpy(out,
               rb.data + readFrame * channels,
               firstSamples * sizeof(float));

        memcpy(out + firstSamples,
               rb.data,
               secondSamples * sizeof(float));
    }

    rb.read.store((readFrame + inNumberFrames) % rb.frames,
                  std::memory_order_release);

    return noErr;
}

extern "C" {

// ======================================================
// open() - получаем информацию о формате
// ======================================================

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1open
(JNIEnv* env, jobject noad, jstring jDeviceId, jobject format) {
    env->GetJavaVM(&javaVM);
    nativeOutputDeivce = env->NewGlobalRef(noad);

    if (ctx) return;
    ctx = new AudioContext();

    jclass fmtCls = env->GetObjectClass(format);

    // Получаем стандартные параметры
    ctx->sampleRate = env->CallIntMethod(format,
        env->GetMethodID(fmtCls, "sampleRate", "()I"));
    ctx->channels = env->CallIntMethod(format,
        env->GetMethodID(fmtCls, "channels", "()I"));

    // Получаем bits per sample
    jmethodID bitsPerSampleMethod = env->GetMethodID(fmtCls, "bitsPerSample", "()I");
    ctx->bitsPerSample = env->CallIntMethod(format, bitsPerSampleMethod);
    ctx->bytesPerSample = (ctx->bitsPerSample + 7) / 8;

    // Получаем signed/unsigned
    jmethodID signedMethod = env->GetMethodID(fmtCls, "signed", "()Z");
    ctx->isSigned = env->CallBooleanMethod(format, signedMethod);

    // Получаем byte order
    jmethodID byteOrderMethod = env->GetMethodID(fmtCls, "byteOrder", "()Ljava/nio/ByteOrder;");
    jobject byteOrderObj = env->CallObjectMethod(format, byteOrderMethod);

    if (byteOrderObj) {
        jclass byteOrderCls = env->GetObjectClass(byteOrderObj);
        jmethodID toStringMethod = env->GetMethodID(byteOrderCls, "toString", "()Ljava/lang/String;");
        jstring byteOrderStr = (jstring)env->CallObjectMethod(byteOrderObj, toStringMethod);

        const char* byteOrderCStr = env->GetStringUTFChars(byteOrderStr, nullptr);
        std::string byteOrder(byteOrderCStr);
        ctx->isBigEndian = (byteOrder == "BIG_ENDIAN");
        env->ReleaseStringUTFChars(byteOrderStr, byteOrderCStr);
    }

    // Получаем AudioCodec для определения float форматов
    jmethodID audioCodecMethod = env->GetMethodID(fmtCls, "audioCodec", "()Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;");
    jobject audioCodecObj = env->CallObjectMethod(format, audioCodecMethod);

    if (audioCodecObj) {
        jclass audioCodecCls = env->GetObjectClass(audioCodecObj);
        jmethodID nameMethod = env->GetMethodID(audioCodecCls, "name", "()Ljava/lang/String;");
        jstring codecNameStr = (jstring)env->CallObjectMethod(audioCodecObj, nameMethod);

        const char* codecNameCStr = env->GetStringUTFChars(codecNameStr, nullptr);
        std::string codecName(codecNameCStr);

        // Определяем float форматы
        if (codecName == "FLOAT32" || codecName == "FLOAT64") {
            ctx->isFloatFormat = true;
        } else {
            ctx->isFloatFormat = false;
        }

        env->ReleaseStringUTFChars(codecNameStr, codecNameCStr);
    }

    initConverter(ctx);

    const char* deviceIdStr = env->GetStringUTFChars(jDeviceId, nullptr);
    AudioDeviceID devId = static_cast<AudioDeviceID>(strtoul(deviceIdStr, nullptr, 10));
    env->ReleaseStringUTFChars(jDeviceId, deviceIdStr);

    AudioStreamBasicDescription asbd{};
    asbd.mSampleRate = ctx->sampleRate;
    asbd.mChannelsPerFrame = ctx->channels;
    asbd.mBitsPerChannel = 32;
    asbd.mFramesPerPacket = 1;
    asbd.mBytesPerFrame = ctx->channels * sizeof(float);
    asbd.mBytesPerPacket = asbd.mBytesPerFrame;
    asbd.mFormatID = kAudioFormatLinearPCM;
    asbd.mFormatFlags = kAudioFormatFlagIsFloat | kAudioFormatFlagIsPacked;

    ctx->rb.frames = ctx->sampleRate * 5;
    ctx->rb.channels = ctx->channels;
    ctx->rb.data = static_cast<float*>(malloc(ctx->rb.frames * ctx->channels * sizeof(float)));
    ctx->rb.read.store(0);
    ctx->rb.write.store(0);

    AudioComponentDescription desc{};
    desc.componentType = kAudioUnitType_Output;
    desc.componentSubType = kAudioUnitSubType_HALOutput;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;

    AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
    AudioComponentInstanceNew(comp, &ctx->unit);

    // Привязка к устройству
    AudioUnitSetProperty(ctx->unit,
                         kAudioOutputUnitProperty_CurrentDevice,
                         kAudioUnitScope_Global,
                         0,
                         &devId,
                         sizeof(devId));

    AudioUnitSetProperty(ctx->unit,
                         kAudioUnitProperty_StreamFormat,
                         kAudioUnitScope_Input,
                         0,
                         &asbd,
                         sizeof(asbd));

    AURenderCallbackStruct cb{};
    cb.inputProc = audioRenderCallback;
    cb.inputProcRefCon = ctx;
    AudioUnitSetProperty(ctx->unit,
                         kAudioUnitProperty_SetRenderCallback,
                         kAudioUnitScope_Input,
                         0,
                         &cb,
                         sizeof(cb));

    AudioUnitInitialize(ctx->unit);
    ctx->running.store(true, std::memory_order_release);
    AudioOutputUnitStart(ctx->unit);
}

// ======================================================
// write() — Поддержка любых PCM форматов
// ======================================================

JNIEXPORT jint JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1write
(JNIEnv* env, jobject, jobject buffer) {
    if (!ctx || !ctx->running.load(std::memory_order_acquire)) {
        return 0;
    }

    // Быстрый доступ к буферу
    const uint8_t* byteData = static_cast<const uint8_t*>(
        env->GetDirectBufferAddress(buffer));
    const jlong capacity = env->GetDirectBufferCapacity(buffer);

    if (!byteData || capacity <= 0) {
        return 0;
    }

    // Используем предвычисленные значения
    const uint32_t bytesPerFrame = ctx->bytesPerFrame;
    const uint32_t frames = static_cast<uint32_t>(capacity / bytesPerFrame);

    if (frames == 0) {
        return 0;
    }

    RingBuffer& rb = ctx->rb;

    // Быстрая проверка свободного места (без вызова функций)
    const uint32_t writePos = rb.write.load(std::memory_order_relaxed);
    const uint32_t readPos = rb.read.load(std::memory_order_acquire);

    uint32_t freeFrames;
    if (writePos >= readPos) {
        freeFrames = rb.frames - (writePos - readPos) - 1;
    } else {
        freeFrames = readPos - writePos - 1;
    }

    if (freeFrames == 0) {
        return 0;
    }

    const uint32_t framesToWrite = (frames < freeFrames) ? frames : freeFrames;
    if (framesToWrite == 0) {
        return 0;
    }

    // Вычисляем указатель на место записи
    float* dstPtr = &rb.data[writePos * rb.channels];

    // Используем предвычисленный конвертер
    if (ctx->converter) {
        ctx->converter(byteData, dstPtr, framesToWrite,
                      ctx->channels, ctx->scale);
    } else {
        // Fallback (не должно происходить)
        memset(dstPtr, 0, framesToWrite * ctx->channels * sizeof(float));
    }

    // Обновляем позицию записи
    uint32_t newWritePos = writePos + framesToWrite;
    if (newWritePos >= rb.frames) {
        newWritePos -= rb.frames;
    }
    rb.write.store(newWritePos, std::memory_order_release);

    return framesToWrite;
}

// ======================================================
// flush()
// ======================================================

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1flush
(JNIEnv*, jobject) {
    // intentionally no-op
}

// ======================================================
// close()
// ======================================================

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1close
(JNIEnv*, jobject, jstring) {

    if (!ctx) return;

    ctx->running.store(false, std::memory_order_release);

    if (ctx->unit) {
        AudioOutputUnitStop(ctx->unit);
        AudioUnitUninitialize(ctx->unit);
        AudioComponentInstanceDispose(ctx->unit);
    }

    if (ctx->rb.data) {
        free(ctx->rb.data);
    }

    delete ctx;
    ctx = nullptr;
}

} // extern "C"