#include <AudioToolbox/AudioToolbox.h>
#include <CoreAudio/CoreAudioTypes.h>
#include <AudioUnit/AudioUnit.h>
#include <AudioUnit/AudioComponent.h>
#include <AudioUnit/AudioUnitProperties.h>
#include <AudioUnit/AudioOutputUnit.h>
#include <jni.h>
#include <vector>
#include <mutex>
#include <cstring>

#include "org_plovdev_audioengine_devices_NativeOutputAudioDevice.h"

struct AudioContext {
    AudioUnit unit = nullptr;
    std::vector<uint8_t> ringBuffer;
    size_t writePos = 0;
    size_t readPos = 0;
    size_t bufferSize = 0;
    std::mutex mtx;
    bool running = false;
    JavaVM* jvm = nullptr;
    jobject chunkProviderGlobal = nullptr;
};

static AudioContext* ctx = nullptr;

// Вспомогательная функция для вызова Java ChunkProvider
static void fillBufferFromProvider(uint8_t* out, size_t totalBytes) {
    if (!ctx || !ctx->chunkProviderGlobal) {
        std::memset(out, 0, totalBytes);
        return;
    }

    JNIEnv* env = nullptr;
    ctx->jvm->AttachCurrentThread((void**)&env, nullptr);

    jclass cls = env->GetObjectClass(ctx->chunkProviderGlobal);
    jmethodID mid = env->GetMethodID(cls, "onNextChunkRequired", "(I)Ljava/nio/ByteBuffer;");
    jobject buf = env->CallObjectMethod(ctx->chunkProviderGlobal, mid, (jint)totalBytes);

    if (buf != nullptr) {
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(buf);
        std::memcpy(out, data, totalBytes);
    } else {
        std::memset(out, 0, totalBytes);
    }
}

static OSStatus audioRenderCallback(void* inRefCon,
                                    AudioUnitRenderActionFlags* ioActionFlags,
                                    const AudioTimeStamp* inTimeStamp,
                                    UInt32 inBusNumber,
                                    UInt32 inNumberFrames,
                                    AudioBufferList* ioData) {

    AudioContext* ctx = (AudioContext*)inRefCon;
    size_t bytesPerFrame = ioData->mBuffers[0].mDataByteSize / inNumberFrames;
    size_t totalBytes = inNumberFrames * bytesPerFrame;
    uint8_t* out = (uint8_t*)ioData->mBuffers[0].mData;

    std::unique_lock<std::mutex> lock(ctx->mtx);

    // Копируем из кольцевого буфера, если есть данные
    size_t bytesCopied = 0;
    while (bytesCopied < totalBytes) {
        if (ctx->readPos != ctx->writePos) {
            out[bytesCopied] = ctx->ringBuffer[ctx->readPos];
            ctx->readPos = (ctx->readPos + 1) % ctx->bufferSize;
            bytesCopied++;
        } else {
            // Если данных нет — дергаем provider для оставшейся части
            fillBufferFromProvider(&out[bytesCopied], totalBytes - bytesCopied);
            ctx->readPos = ctx->writePos; // синхронизируем позиции
            break;
        }
    }

    return noErr;
}

extern "C" {

JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1open
(JNIEnv* env, jobject jobj, jstring deviceId, jobject format) {

    if (!ctx) ctx = new AudioContext();

    env->GetJavaVM(&ctx->jvm);

    // Получаем параметры TrackFormat
    jclass clsTrackFormat = env->GetObjectClass(format);
    jint sampleRate = env->CallIntMethod(format, env->GetMethodID(clsTrackFormat, "sampleRate", "()I"));
    jint channels = env->CallIntMethod(format, env->GetMethodID(clsTrackFormat, "channels", "()I"));
    jint bitsPerSample = env->CallIntMethod(format, env->GetMethodID(clsTrackFormat, "bitsPerSample", "()I"));

    AudioStreamBasicDescription asbd{};
    asbd.mSampleRate = sampleRate;
    asbd.mChannelsPerFrame = channels;
    asbd.mBitsPerChannel = bitsPerSample;
    asbd.mBytesPerFrame = channels * (bitsPerSample / 8);
    asbd.mFramesPerPacket = 1;
    asbd.mBytesPerPacket = asbd.mBytesPerFrame;
    asbd.mFormatID = kAudioFormatLinearPCM;
    asbd.mFormatFlags = kAudioFormatFlagIsPacked | kAudioFormatFlagIsSignedInteger;

    ctx->bufferSize = asbd.mBytesPerFrame * (size_t)(asbd.mSampleRate * 2); // 2 секунды
    ctx->ringBuffer.resize(ctx->bufferSize);
    ctx->readPos = ctx->writePos = 0;
    ctx->running = true;

AudioComponentDescription desc{};
    desc.componentType = kAudioUnitType_Output;
    desc.componentSubType = kAudioUnitSubType_DefaultOutput;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;

    AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
    AudioComponentInstanceNew(comp, &ctx->unit);

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
    AudioOutputUnitStart(ctx->unit);
}

JNIEXPORT jint JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1write
(JNIEnv* env, jobject jobj, jobject buffer) {

    if (!ctx || !ctx->running) return 0;

    uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(buffer);
    jlong size = env->GetDirectBufferCapacity(buffer);

    std::unique_lock<std::mutex> lock(ctx->mtx);

    for (jlong i = 0; i < size; ++i) {
        ctx->ringBuffer[ctx->writePos] = data[i];
        ctx->writePos = (ctx->writePos + 1) % ctx->bufferSize;
        if (ctx->writePos == ctx->readPos) ctx->readPos = (ctx->readPos + 1) % ctx->bufferSize;
    }

    return (jint)size;
}

JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1setProvider
(JNIEnv* env, jobject jobj, jobject provider) {
    if (!ctx) return;
    if (ctx->chunkProviderGlobal) env->DeleteGlobalRef(ctx->chunkProviderGlobal);
    ctx->chunkProviderGlobal = env->NewGlobalRef(provider);
}

JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1flush
  (JNIEnv *, jobject) {
    // не нужно ничего, буфер сам поддерживает непрерывность
}

JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1close
(JNIEnv* env, jobject jobj, jstring deviceId) {
    if (!ctx) return;

    ctx->running = false;

    if (ctx->unit) {
        AudioOutputUnitStop(ctx->unit);
        AudioUnitUninitialize(ctx->unit);
        AudioComponentInstanceDispose(ctx->unit);
        ctx->unit = nullptr;
    }

    if (ctx->chunkProviderGlobal) env->DeleteGlobalRef(ctx->chunkProviderGlobal);
    ctx->chunkProviderGlobal = nullptr;

    delete ctx;
    ctx = nullptr;
}

} // extern "C"