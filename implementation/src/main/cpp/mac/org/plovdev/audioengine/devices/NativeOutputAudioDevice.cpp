#include <AudioToolbox/AudioToolbox.h>
#include <jni.h>
#include <atomic>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <cstring>
#include <cstdlib>
#include <unordered_map>
#include <memory>
#include <iostream>

JavaVM* g_vm = nullptr;

struct NativeOutputContext {
    AudioUnit unit = nullptr;
    AudioConverterRef converter = nullptr;

    AudioStreamBasicDescription src{};
    AudioStreamBasicDescription dst{};

    std::vector<float> ring;
    size_t ringFrames = 0;

    std::atomic<size_t> readFrame{0};
    std::atomic<size_t> writeFrame{0};

    std::mutex mtx;
    std::condition_variable canWrite;

    bool running = false;
};

std::unordered_map<long, std::unique_ptr<NativeOutputContext>> contexts;
std::mutex contexts_mutex;
long nextContextId = 1;

NativeOutputContext* getContext(long handle) {
    std::lock_guard<std::mutex> lock(contexts_mutex);
    auto it = contexts.find(handle);
    return (it != contexts.end()) ? it->second.get() : nullptr;
}

#pragma mark ======================
#pragma mark CoreAudio callback
#pragma mark ======================


OSStatus renderCallback(
        void* refCon,
        AudioUnitRenderActionFlags*,
        const AudioTimeStamp*,
        UInt32,
        UInt32 frames,
        AudioBufferList* ioData)
{
    auto* c = static_cast<NativeOutputContext*>(refCon);
    size_t ch = c->dst.mChannelsPerFrame;

    size_t available = c->writeFrame.load(std::memory_order_acquire) -
                      c->readFrame.load(std::memory_order_acquire);
    size_t toRead = std::min<size_t>(available, frames);

    // В зависимости от формата используем правильный тип
    if (c->dst.mFormatFlags & kAudioFormatFlagIsFloat) {
        float* out = (float*) ioData->mBuffers[0].mData;

        for (size_t i = 0; i < toRead; i++) {
            size_t idx = ((c->readFrame + i) % c->ringFrames) * ch;
            memcpy(out + i * ch, &c->ring[idx], ch * sizeof(float));
        }

        if (toRead < frames) {
            memset(out + toRead * ch, 0, (frames - toRead) * ch * sizeof(float));
        }
    } else {
        // Для integer PCM
        uint8_t* out = (uint8_t*) ioData->mBuffers[0].mData;
        size_t bytesPerSample = c->dst.mBitsPerChannel / 8;
        size_t bytesPerFrame = ch * bytesPerSample;

        for (size_t i = 0; i < toRead; i++) {
            size_t idx = ((c->readFrame + i) % c->ringFrames) * ch;
            memcpy(out + i * bytesPerFrame, &c->ring[idx], bytesPerFrame);
        }

        if (toRead < frames) {
            memset(out + toRead * bytesPerFrame, 0, (frames - toRead) * bytesPerFrame);
        }
    }

    c->readFrame.fetch_add(toRead, std::memory_order_release);
    c->canWrite.notify_one();
    return noErr;
}

#pragma mark ======================
#pragma mark JNI METHODS
#pragma mark ======================

extern "C" {
JNIEXPORT jlong JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1open
(JNIEnv* env, jobject, jobject fmt, jobject deviceInfo)
{
    env->GetJavaVM(&g_vm);

    auto ctx = std::make_unique<NativeOutputContext>();
    NativeOutputContext* ctxPtr = ctx.get();

    jclass infoCls = env->GetObjectClass(deviceInfo);
    jstring jDevId = (jstring) env->CallObjectMethod(deviceInfo, env->GetMethodID(infoCls, "id", "()Ljava/lang/String;"));

    jclass fmtCls = env->GetObjectClass(fmt);

    // Получаем параметры
    int sr = env->CallIntMethod(fmt, env->GetMethodID(fmtCls, "sampleRate", "()I"));
    int ch = env->CallIntMethod(fmt, env->GetMethodID(fmtCls, "channels", "()I"));
    int bits = env->CallIntMethod(fmt, env->GetMethodID(fmtCls, "bitDepth", "()I"));
    jboolean signedPcm = env->CallBooleanMethod(fmt, env->GetMethodID(fmtCls, "signed", "()Z"));

    jmethodID audioCodecMethod = env->GetMethodID(fmtCls, "audioCodec", "()Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;");
    jobject audioCodecObj = env->CallObjectMethod(fmt, audioCodecMethod);
    jclass audioCodecClass = env->GetObjectClass(audioCodecObj);
    jmethodID nameMethod = env->GetMethodID(audioCodecClass, "name", "()Ljava/lang/String;");

    jstring codecName = (jstring) env->CallObjectMethod(audioCodecObj, nameMethod);

    const char* codecStr = env->GetStringUTFChars(codecName, NULL);
    std::string codec(codecStr);
    env->ReleaseStringUTFChars(codecName, codecStr);

    // Настраиваем формат
    ctx->src.mSampleRate = sr;
    ctx->src.mFormatID = kAudioFormatLinearPCM;
    ctx->src.mChannelsPerFrame = ch;
    ctx->src.mFramesPerPacket = 1;

    if (codec == "FLOAT32") {
        ctx->src.mFormatFlags = kAudioFormatFlagIsFloat | kAudioFormatFlagIsPacked;
        ctx->src.mBitsPerChannel = 32;
    } else if (codec == "FLOAT64") {
        ctx->src.mFormatFlags = kAudioFormatFlagIsFloat | kAudioFormatFlagIsPacked;
        ctx->src.mBitsPerChannel = 64;
    } else if (codec == "PCM8") {
        ctx->src.mFormatFlags = kLinearPCMFormatFlagIsPacked;
        if (signedPcm) ctx->src.mFormatFlags |= kLinearPCMFormatFlagIsSignedInteger;
        ctx->src.mBitsPerChannel = 8;
    } else if (codec == "PCM16") {
        ctx->src.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger | kLinearPCMFormatFlagIsPacked;
        ctx->src.mBitsPerChannel = 16;
    } else if (codec == "PCM24") {
        ctx->src.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger | kLinearPCMFormatFlagIsPacked;
        ctx->src.mBitsPerChannel = 24;
    } else if (codec == "PCM32") {
        ctx->src.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger | kLinearPCMFormatFlagIsPacked;
        ctx->src.mBitsPerChannel = 32;
    } else {
        std::cerr << "Unsupported codec: " << codec << std::endl;
        return 0;
    }

    ctx->src.mBytesPerFrame = ch * (ctx->src.mBitsPerChannel / 8);
    ctx->src.mBytesPerPacket = ctx->src.mBytesPerFrame;

    ctx->dst = ctx->src; // Просто копируем!

    ctx->converter = nullptr;

    const char* devStr = env->GetStringUTFChars(jDevId, nullptr);
    AudioDeviceID devId = (AudioDeviceID) strtoul(devStr, nullptr, 10);
    env->ReleaseStringUTFChars(jDevId, devStr);

    ctx->ringFrames = sr;
    ctx->ring.resize(ctx->ringFrames * ch);

    AudioComponentDescription desc{};
    desc.componentType = kAudioUnitType_Output;
    desc.componentSubType = kAudioUnitSubType_HALOutput;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;

    AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
    AudioComponentInstanceNew(comp, &ctx->unit);

    // Устанавливаем устройство
    AudioUnitSetProperty(ctx->unit,
        kAudioOutputUnitProperty_CurrentDevice,
        kAudioUnitScope_Global, 0,
        &devId, sizeof(devId));

    AudioUnitSetProperty(ctx->unit,
        kAudioUnitProperty_StreamFormat,
        kAudioUnitScope_Input, 0,
        &ctx->dst, sizeof(ctx->dst));

    // Callback
    AURenderCallbackStruct cb{};
    cb.inputProc = renderCallback;
    cb.inputProcRefCon = ctxPtr;

    AudioUnitSetProperty(ctx->unit,
        kAudioUnitProperty_SetRenderCallback,
        kAudioUnitScope_Input, 0,
        &cb, sizeof(cb));

    AudioUnitInitialize(ctx->unit);
    AudioOutputUnitStart(ctx->unit);
    ctx->running = true;

    std::lock_guard<std::mutex> lock(contexts_mutex);
    long handle = nextContextId++;
    contexts[handle] = std::move(ctx);

    env->DeleteLocalRef(infoCls);
    env->DeleteLocalRef(jDevId);
    env->DeleteLocalRef(fmtCls);
    env->DeleteLocalRef(audioCodecObj);
    env->DeleteLocalRef(audioCodecClass);
    env->DeleteLocalRef(codecName);
    return handle;
}

JNIEXPORT jint JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1write
(JNIEnv* env, jobject, jobject buffer, jlong handle) {
    auto* ctx = getContext(handle);
    if (!ctx || !ctx->running) return 0;

    auto* src = (uint8_t*) env->GetDirectBufferAddress(buffer);
    jlong cap = env->GetDirectBufferCapacity(buffer);
    size_t frames = cap / ctx->src.mBytesPerFrame;
    size_t ch = ctx->src.mChannelsPerFrame;

    std::unique_lock<std::mutex> lock(ctx->mtx);

    while (frames > 0) {
        size_t used = ctx->writeFrame.load(std::memory_order_acquire) -
                     ctx->readFrame.load(std::memory_order_acquire);
        size_t freeFrames = ctx->ringFrames - used;

        while (freeFrames == 0) {
            ctx->canWrite.wait(lock);
            used = ctx->writeFrame.load(std::memory_order_acquire) -
                  ctx->readFrame.load(std::memory_order_acquire);
            freeFrames = ctx->ringFrames - used;
        }

        size_t toWrite = std::min(frames, freeFrames);

        // ПРЯМОЕ КОПИРОВАНИЕ! Никакой конвертации!
        for (size_t i = 0; i < toWrite; i++) {
            size_t idx = ((ctx->writeFrame + i) % ctx->ringFrames) * ch;
            memcpy(&ctx->ring[idx],
                   src + i * ctx->src.mBytesPerFrame,
                   ctx->src.mBytesPerFrame);
        }

        ctx->writeFrame.fetch_add(toWrite, std::memory_order_release);
        src += toWrite * ctx->src.mBytesPerFrame;
        frames -= toWrite;
    }

    return cap;
}

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1close
(JNIEnv*, jobject, jlong handle)
{
    auto* ctx = getContext(handle);
    if (!ctx) return;

    ctx->running = false;
    AudioOutputUnitStop(ctx->unit);
    AudioUnitUninitialize(ctx->unit);
    AudioComponentInstanceDispose(ctx->unit);
    AudioConverterDispose(ctx->converter);

    std::lock_guard<std::mutex> lock(contexts_mutex);
    contexts.erase(handle);
}
} // extern "C"