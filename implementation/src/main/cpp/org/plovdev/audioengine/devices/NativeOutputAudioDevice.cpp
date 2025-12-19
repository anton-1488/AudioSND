#include <AudioToolbox/AudioToolbox.h>
#include <AudioUnit/AudioUnit.h>
#include <jni.h>

#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <cstring>

#include "org_plovdev_audioengine_devices_NativeOutputAudioDevice.h"

// ======================================================
// RingBuffer (lock-free, frames-based, float32)
// ======================================================

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

    std::atomic<bool> running{false};
    std::atomic<uint64_t> underruns{0};
};

static AudioContext* ctx = nullptr;

// ======================================================
// CoreAudio render callback (REALTIME SAFE)
// ======================================================

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
        memset(out, 0, samplesRequested * sizeof(float));
        c->underruns.fetch_add(1, std::memory_order_relaxed);
        return noErr;
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
// open()
// ======================================================

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1open
(JNIEnv* env, jobject, jstring, jobject format) {

    if (ctx) return;
    ctx = new AudioContext();

    jclass fmtCls = env->GetObjectClass(format);

    ctx->sampleRate = env->CallIntMethod(
        format, env->GetMethodID(fmtCls, "sampleRate", "()I"));

    ctx->channels = env->CallIntMethod(
        format, env->GetMethodID(fmtCls, "channels", "()I"));

    // ===============================
    // AudioStreamBasicDescription
    // ===============================

AudioStreamBasicDescription asbd{};
    asbd.mSampleRate = ctx->sampleRate;
    asbd.mChannelsPerFrame = ctx->channels;
    asbd.mBitsPerChannel = 32;
    asbd.mFramesPerPacket = 1;
    asbd.mBytesPerFrame = ctx->channels * sizeof(float);
    asbd.mBytesPerPacket = asbd.mBytesPerFrame;
    asbd.mFormatID = kAudioFormatLinearPCM;
    asbd.mFormatFlags =
        kAudioFormatFlagIsFloat |
        kAudioFormatFlagIsPacked;

    // ===============================
    // RingBuffer: 5 seconds
    // ===============================

    ctx->rb.frames = ctx->sampleRate * 5;
    ctx->rb.channels = ctx->channels;
    ctx->rb.data = static_cast<float*>(
        malloc(ctx->rb.frames * ctx->channels * sizeof(float)));

    ctx->rb.read.store(0);
    ctx->rb.write.store(0);

    // ===============================
    // AudioUnit setup
    // ===============================

    AudioComponentDescription desc{};
    desc.componentType = kAudioUnitType_Output;
    desc.componentSubType = kAudioUnitSubType_DefaultOutput;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;

    AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
    AudioComponentInstanceNew(comp, &ctx->unit);

    AudioUnitSetProperty(
        ctx->unit,
        kAudioUnitProperty_StreamFormat,
        kAudioUnitScope_Input,
        0,
        &asbd,
        sizeof(asbd));

    AURenderCallbackStruct cb{};
    cb.inputProc = audioRenderCallback;
    cb.inputProcRefCon = ctx;

    AudioUnitSetProperty(
        ctx->unit,
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
// write() — PCM16 → float32
// ======================================================

JNIEXPORT jint JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1write
(JNIEnv* env, jobject, jobject buffer) {

    if (!ctx || !ctx->running.load(std::memory_order_relaxed)) return 0;

    auto* pcm = static_cast<int16_t*>(
        env->GetDirectBufferAddress(buffer));

    jlong bytes = env->GetDirectBufferCapacity(buffer);
    if (!pcm || bytes <= 0) return 0;

    uint32_t frames =
        static_cast<uint32_t>(
            bytes / (sizeof(int16_t) * ctx->channels));

    RingBuffer& rb = ctx->rb;
    uint32_t freeFrames = rbFree(rb);
    uint32_t framesToWrite =
        (frames < freeFrames) ? frames : freeFrames;

    uint32_t writeFrame = rb.write.load(std::memory_order_relaxed);

    for (uint32_t f = 0; f < framesToWrite; ++f) {
        uint32_t dstFrame = writeFrame % rb.frames;
        uint32_t dstBase = dstFrame * rb.channels;
        uint32_t srcBase = f * rb.channels;

        for (uint32_t ch = 0; ch < rb.channels; ++ch) {
            rb.data[dstBase + ch] =
                static_cast<float>(pcm[srcBase + ch]) / 32768.0f;
        }
        writeFrame++;
    }

    rb.write.store(writeFrame % rb.frames,
                   std::memory_order_release);

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