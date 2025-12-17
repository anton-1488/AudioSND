#include <AudioToolbox/AudioToolbox.h>
#include <CoreAudio/CoreAudioTypes.h>
#include <AudioUnit/AudioUnit.h>
#include <AudioUnit/AudioComponent.h>
#include <AudioUnit/AudioUnitProperties.h>
#include <AudioUnit/AudioOutputUnit.h>
#include <jni.h>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <cstring>

#include "org_plovdev_audioengine_devices_NativeOutputAudioDevice.h"

struct AudioContext {
    AudioUnit unit = nullptr;
    std::vector<uint8_t> ringBuffer;
    size_t writePos = 0;
    size_t readPos = 0;
    size_t bufferSize = 0;
    std::mutex mtx;
    std::condition_variable cv;
    bool running = false;
};

static AudioContext* ctx = nullptr;

static OSStatus audioRenderCallback(void* inRefCon,
                                    AudioUnitRenderActionFlags* ioActionFlags,
                                    const AudioTimeStamp* inTimeStamp,
                                    UInt32 inBusNumber,
                                    UInt32 inNumberFrames,
                                    AudioBufferList* ioData) {
    AudioContext* ctx = (AudioContext*)inRefCon;

    size_t bytesPerFrame = ioData->mBuffers[0].mDataByteSize / inNumberFrames;
    size_t totalBytes = inNumberFrames * bytesPerFrame;

    std::unique_lock<std::mutex> lock(ctx->mtx);

    uint8_t* out = (uint8_t*)ioData->mBuffers[0].mData;

    for (size_t i = 0; i < totalBytes; ++i) {
        if (ctx->readPos != ctx->writePos) {
            out[i] = ctx->ringBuffer[ctx->readPos];
            ctx->readPos = (ctx->readPos + 1) % ctx->bufferSize;
        } else {
            out[i] = 0; // тишина, если буфер пуст
        }
    }

    return noErr;
}

extern "C" {

JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1open
  (JNIEnv* env, jobject jobj, jstring deviceId, jobject format) {

    if (!ctx) ctx = new AudioContext();

    // Найти классы и методы
    jclass clsTrackFormat = env->GetObjectClass(format);
    jmethodID midSampleRate = env->GetMethodID(clsTrackFormat, "sampleRate", "()I");
    jmethodID midChannels = env->GetMethodID(clsTrackFormat, "channels", "()I");
    jmethodID midBitsPerSample = env->GetMethodID(clsTrackFormat, "bitsPerSample", "()I");
    jmethodID midSigned = env->GetMethodID(clsTrackFormat, "signed", "()Z");
    jmethodID midByteOrder = env->GetMethodID(clsTrackFormat, "byteOrder", "()Ljava/nio/ByteOrder;");

    // Вызвать методы
    jint sampleRate = env->CallIntMethod(format, midSampleRate);
    jint channels = env->CallIntMethod(format, midChannels);
    jint bitsPerSample = env->CallIntMethod(format, midBitsPerSample);
    jboolean isSigned = env->CallBooleanMethod(format, midSigned);
    jobject byteOrderObj = env->CallObjectMethod(format, midByteOrder);

    // Определяем порядок байтов
    jclass clsByteOrder = env->FindClass("java/nio/ByteOrder");
    jfieldID fidBIG = env->GetStaticFieldID(clsByteOrder, "BIG_ENDIAN", "Ljava/nio/ByteOrder;");
    jobject bigEndian = env->GetStaticObjectField(clsByteOrder, fidBIG);
    jboolean isBigEndian = env->IsSameObject(byteOrderObj, bigEndian);

    // Создаем AudioStreamBasicDescription
    AudioStreamBasicDescription asbd{};
    asbd.mSampleRate = sampleRate;
    asbd.mChannelsPerFrame = channels;
    asbd.mBitsPerChannel = bitsPerSample;
    asbd.mBytesPerFrame = channels * (bitsPerSample / 8);
    asbd.mFramesPerPacket = 1;
    asbd.mBytesPerPacket = asbd.mBytesPerFrame;
    asbd.mFormatID = kAudioFormatLinearPCM;
    asbd.mFormatFlags = kAudioFormatFlagIsPacked;
    if (isSigned) asbd.mFormatFlags |= kAudioFormatFlagIsSignedInteger;
    if (isBigEndian) {
        asbd.mFormatFlags |= kAudioFormatFlagIsBigEndian;
    } else {
        //asbd.mFormatFlags |= kAudioFormatFlagIsLittleEndian;
    }

    ctx->bufferSize = asbd.mBytesPerFrame * 44100; // 1 сек кольцевой буфер
    ctx->ringBuffer.resize(ctx->bufferSize);
    ctx->writePos = 0;
    ctx->readPos = 0;
    ctx->running = true;

    // Создание RemoteIO AudioUnit
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

        // Если write догнал read, отрезаем старые данные (или можно блокировать)
        if (ctx->writePos == ctx->readPos) {
            ctx->readPos = (ctx->readPos + 1) % ctx->bufferSize;
        }
    }

    return (jint)size;
}
JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1flush
  (JNIEnv* env, jobject jobj) {
    // Можно ничего не делать, кольцевой буфер сам поддерживает непрерывность
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

    delete ctx;
    ctx = nullptr;
}

} // extern "C"