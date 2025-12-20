#include <AudioToolbox/AudioToolbox.h>
#include <AudioUnit/AudioUnit.h>
#include <jni.h>

#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <cstring>

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
    return (rb.write.load(std::memory_order_acquire)
          - rb.read.load(std::memory_order_acquire)) % rb.frames;
}

static inline uint32_t rbFree(const RingBuffer& rb) {
    return rb.frames - rbAvailable(rb) - 1;
}

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

    std::atomic<bool> running{false};
    std::atomic<uint64_t> underruns{0};
};

static AudioContext* ctx = nullptr;


// ======================================================
// Вспомогательные функции конвертации
// ======================================================

static float convertPCM8(uint8_t sample, bool isSigned) {
    if (isSigned) {
        // signed 8-bit: -128 to 127
        int8_t signedSample = static_cast<int8_t>(sample);
        return static_cast<float>(signedSample) / 128.0f;
    } else {
        // unsigned 8-bit: 0 to 255 → -1.0 to 1.0
        return (static_cast<float>(sample) - 128.0f) / 127.0f;
    }
}

static float convertPCM16(const uint8_t* data, bool isBigEndian) {
    int16_t sample;
    if (isBigEndian) {
        sample = (data[0] << 8) | data[1];
    } else {
        sample = (data[1] << 8) | data[0];
    }
    return static_cast<float>(sample) / 32768.0f;
}

static float convertPCM24(const uint8_t* data, bool isBigEndian) {
    int32_t sample;
    if (isBigEndian) {
        sample = (data[0] << 16) | (data[1] << 8) | data[2];
    } else {
        sample = (data[2] << 16) | (data[1] << 8) | data[0];
    }

    // Sign extend 24-bit to 32-bit
    if (sample & 0x800000) {
        sample |= 0xFF000000;
    }

    return static_cast<float>(sample) / 8388608.0f;
}

static float convertPCM32(const uint8_t* data, bool isBigEndian) {
    int32_t sample;
    if (isBigEndian) {
        sample = (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3];
    } else {
        sample = (data[3] << 24) | (data[2] << 16) | (data[1] << 8) | data[0];
    }
    return static_cast<float>(sample) / 2147483648.0f;
}

static float convertFloat32(const uint8_t* data, bool isBigEndian) {
    union {
        uint32_t i;
        float f;
    } u;

    if (isBigEndian) {
        u.i = (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3];
    } else {
        u.i = (data[3] << 24) | (data[2] << 16) | (data[1] << 8) | data[0];
    }

    return u.f;
}

static float convertFloat64(const uint8_t* data, bool isBigEndian) {
    union {
        uint64_t i;
        double d;
    } u;

    if (isBigEndian) {
        u.i = (static_cast<uint64_t>(data[0]) << 56) |
              (static_cast<uint64_t>(data[1]) << 48) |
              (static_cast<uint64_t>(data[2]) << 40) |
              (static_cast<uint64_t>(data[3]) << 32) |
              (static_cast<uint64_t>(data[4]) << 24) |
              (static_cast<uint64_t>(data[5]) << 16) |
              (static_cast<uint64_t>(data[6]) << 8) |
              static_cast<uint64_t>(data[7]);
    } else {
        u.i = (static_cast<uint64_t>(data[7]) << 56) |
              (static_cast<uint64_t>(data[6]) << 48) |
              (static_cast<uint64_t>(data[5]) << 40) |
              (static_cast<uint64_t>(data[4]) << 32) |
              (static_cast<uint64_t>(data[3]) << 24) |
              (static_cast<uint64_t>(data[2]) << 16) |
              (static_cast<uint64_t>(data[1]) << 8) |
              static_cast<uint64_t>(data[0]);
    }

    return static_cast<float>(u.d);
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

    if (availableFrames < inNumberFrames) {
        jint requestBytes = (inNumberFrames - availableFrames) * c->channels * sizeof(float);
        requestChunkProviderMethod(requestBytes);
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

    ctx->rb.frames = ctx->sampleRate * 20;
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
    if (!ctx || !ctx->running.load(std::memory_order_relaxed)) return 0;

    void* rawData = env->GetDirectBufferAddress(buffer);
    jlong bytes = env->GetDirectBufferCapacity(buffer);

    if (!rawData || bytes <= 0) return 0;

    const uint8_t* byteData = static_cast<const uint8_t*>(rawData);
    uint32_t bytesPerSample = (ctx->bitsPerSample + 7) / 8; // округляем до байт
    uint32_t bytesPerFrame = bytesPerSample * ctx->channels;

    // Рассчитываем количество фреймов
    uint32_t frames = static_cast<uint32_t>(bytes / bytesPerFrame);
    if (frames == 0) return 0;

    RingBuffer& rb = ctx->rb;
    uint32_t freeFrames = rbFree(rb);
    uint32_t framesToWrite = (frames < freeFrames) ? frames : freeFrames;

    if (framesToWrite == 0) return 0;

    uint32_t writeFrame = rb.write.load(std::memory_order_relaxed);

    // Основной цикл конвертации
    for (uint32_t f = 0; f < framesToWrite; ++f) {
        uint32_t dstFrame = writeFrame % rb.frames;
        uint32_t dstBase = dstFrame * rb.channels;
        uint32_t srcBase = f * bytesPerFrame;

        for (uint32_t ch = 0; ch < rb.channels; ++ch) {
            float sample = 0.0f;
            uint32_t sampleOffset = srcBase + (ch * bytesPerSample);

            // Выбираем функцию конвертации в зависимости от формата
            if (ctx->isFloatFormat) {
                if (ctx->bitsPerSample == 32) {
                    sample = convertFloat32(&byteData[sampleOffset], ctx->isBigEndian);
                } else if (ctx->bitsPerSample == 64) {
                    sample = convertFloat64(&byteData[sampleOffset], ctx->isBigEndian);
                }
            } else {
                switch (ctx->bitsPerSample) {
                    case 8:
                        sample = convertPCM8(byteData[sampleOffset], ctx->isSigned);
                        break;
                    case 16:
                        sample = convertPCM16(&byteData[sampleOffset], ctx->isBigEndian);
                        break;
                    case 24:
                        sample = convertPCM24(&byteData[sampleOffset], ctx->isBigEndian);
                        break;
                    case 32:
                        sample = convertPCM32(&byteData[sampleOffset], ctx->isBigEndian);
                        break;
                    default:
                        // Для других форматов возвращаем 0
                        sample = 0.0f;
                        break;
                }
            }

            rb.data[dstBase + ch] = sample;
        }

        writeFrame++;
    }

    rb.write.store(writeFrame % rb.frames, std::memory_order_release);
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