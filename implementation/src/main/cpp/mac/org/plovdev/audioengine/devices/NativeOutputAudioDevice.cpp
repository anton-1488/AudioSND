#include <AudioToolbox/AudioToolbox.h>
#include <jni.h>
#include <atomic>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <cstring>
#include <cstdlib>

static JavaVM* g_vm = nullptr;

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

static NativeOutputContext* ctx = nullptr;

#pragma mark ======================
#pragma mark CoreAudio callback
#pragma mark ======================

static OSStatus renderCallback(
        void* refCon,
        AudioUnitRenderActionFlags*,
        const AudioTimeStamp*,
        UInt32,
        UInt32 frames,
        AudioBufferList* ioData)
{
    auto* c = static_cast<NativeOutputContext*>(refCon);
    float* out = (float*) ioData->mBuffers[0].mData;
    size_t ch = c->dst.mChannelsPerFrame;

    size_t available =
            c->writeFrame.load(std::memory_order_acquire) -
            c->readFrame.load(std::memory_order_acquire);

    size_t toRead = std::min<size_t>(available, frames);

    for (size_t i = 0; i < toRead; i++) {
        size_t idx = ((c->readFrame + i) % c->ringFrames) * ch;
        memcpy(out + i * ch, &c->ring[idx], ch * sizeof(float));
    }

    if (toRead < frames) {
        memset(out + toRead * ch, 0,
               (frames - toRead) * ch * sizeof(float));
    }

    c->readFrame.fetch_add(toRead, std::memory_order_release);
    c->canWrite.notify_one();

    return noErr;
}

#pragma mark ======================
#pragma mark JNI METHODS
#pragma mark ======================

extern "C" {

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1open
(JNIEnv* env, jobject, jobject fmt, jobject deviceInfo)
{
    env->GetJavaVM(&g_vm);
    if (ctx) return;

    ctx = new NativeOutputContext();

    jclass infoCls = env->GetObjectClass(deviceInfo);
    jstring jDevId = (jstring) env->CallObjectMethod(deviceInfo, env->GetMethodID(infoCls, "id", "()Ljava/lang/String;"));;

    jclass fmtCls = env->GetObjectClass(fmt);

    int sr = env->CallIntMethod(fmt, env->GetMethodID(fmtCls, "sampleRate", "()I"));
    int ch = env->CallIntMethod(fmt, env->GetMethodID(fmtCls, "channels", "()I"));
    int bits = env->CallIntMethod(fmt, env->GetMethodID(fmtCls, "bitsPerSample", "()I"));
    jboolean signedPcm = env->CallBooleanMethod(fmt, env->GetMethodID(fmtCls, "signed", "()Z"));

    ctx->src.mSampleRate = sr;
    ctx->src.mFormatID = kAudioFormatLinearPCM;
    ctx->src.mFormatFlags =
        (signedPcm ? kLinearPCMFormatFlagIsSignedInteger : 0) |
        kLinearPCMFormatFlagIsPacked;
    ctx->src.mBitsPerChannel = bits;
    ctx->src.mChannelsPerFrame = ch;
    ctx->src.mFramesPerPacket = 1;
    ctx->src.mBytesPerFrame = ch * (bits / 8);
    ctx->src.mBytesPerPacket = ctx->src.mBytesPerFrame;

    ctx->dst.mSampleRate = sr;
    ctx->dst.mFormatID = kAudioFormatLinearPCM;
    ctx->dst.mFormatFlags =
        kAudioFormatFlagIsFloat | kAudioFormatFlagIsPacked;
    ctx->dst.mBitsPerChannel = 32;
    ctx->dst.mChannelsPerFrame = ch;
    ctx->dst.mFramesPerPacket = 1;
    ctx->dst.mBytesPerFrame = ch * sizeof(float);
    ctx->dst.mBytesPerPacket = ctx->dst.mBytesPerFrame;

    AudioConverterNew(&ctx->src, &ctx->dst, &ctx->converter);

    const char* devStr = env->GetStringUTFChars(jDevId, nullptr);
    AudioDeviceID devId = (AudioDeviceID) strtoul(devStr, nullptr, 10);
    env->ReleaseStringUTFChars(jDevId, devStr);

    ctx->ringFrames = sr * 2;
    ctx->ring.resize(ctx->ringFrames * ch);

    AudioComponentDescription desc{};
    desc.componentType = kAudioUnitType_Output;
    desc.componentSubType = kAudioUnitSubType_HALOutput;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;

    AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
    AudioComponentInstanceNew(comp, &ctx->unit);

    AudioUnitSetProperty(ctx->unit,
        kAudioOutputUnitProperty_CurrentDevice,
        kAudioUnitScope_Global, 0,
        &devId, sizeof(devId));

    AudioUnitSetProperty(ctx->unit,
        kAudioUnitProperty_StreamFormat,
        kAudioUnitScope_Input, 0,
        &ctx->dst, sizeof(ctx->dst));

    AURenderCallbackStruct cb{};
    cb.inputProc = renderCallback;
    cb.inputProcRefCon = ctx;

    AudioUnitSetProperty(ctx->unit,
        kAudioUnitProperty_SetRenderCallback,
        kAudioUnitScope_Input, 0,
        &cb, sizeof(cb));

    AudioUnitInitialize(ctx->unit);
    AudioOutputUnitStart(ctx->unit);
    ctx->running = true;
}

JNIEXPORT jint JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1write
(JNIEnv* env, jobject, jobject buffer)
{
    if (!ctx || !ctx->running) return 0;

    auto* src = (uint8_t*) env->GetDirectBufferAddress(buffer);
    jlong cap = env->GetDirectBufferCapacity(buffer);
    size_t frames = cap / ctx->src.mBytesPerFrame;

    std::unique_lock<std::mutex> lock(ctx->mtx);

    while (frames > 0) {
        size_t used =
            ctx->writeFrame.load() -
            ctx->readFrame.load();
        size_t freeFrames = ctx->ringFrames - used;

        while (freeFrames == 0) {
            ctx->canWrite.wait(lock);
            used = ctx->writeFrame.load() - ctx->readFrame.load();
            freeFrames = ctx->ringFrames - used;
        }

        size_t toWrite = std::min(frames, freeFrames);

        std::vector<float> tmp(toWrite * ctx->dst.mChannelsPerFrame);

        AudioBufferList in{}, out{};
        in.mNumberBuffers = 1;
        in.mBuffers[0].mData = src;
        in.mBuffers[0].mDataByteSize =
            toWrite * ctx->src.mBytesPerFrame;
        in.mBuffers[0].mNumberChannels = ctx->src.mChannelsPerFrame;

        out.mNumberBuffers = 1;
        out.mBuffers[0].mData = tmp.data();
        out.mBuffers[0].mDataByteSize =
            toWrite * ctx->dst.mBytesPerFrame;
        out.mBuffers[0].mNumberChannels = ctx->dst.mChannelsPerFrame;

        AudioConverterConvertComplexBuffer(
            ctx->converter, toWrite, &in, &out);

        for (size_t i = 0; i < toWrite; i++) {
            size_t idx =
                ((ctx->writeFrame + i) % ctx->ringFrames) *
                ctx->dst.mChannelsPerFrame;
            memcpy(&ctx->ring[idx],
                   &tmp[i * ctx->dst.mChannelsPerFrame],
                   ctx->dst.mBytesPerFrame);
        }

        ctx->writeFrame.fetch_add(toWrite);
        src += toWrite * ctx->src.mBytesPerFrame;
        frames -= toWrite;
    }

    return 1;
}

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1flush
(JNIEnv*, jobject)
{
    // no-op
}

JNIEXPORT void JNICALL
Java_org_plovdev_audioengine_devices_NativeOutputAudioDevice__1close
(JNIEnv*, jobject)
{
    if (!ctx) return;

    ctx->running = false;
    AudioOutputUnitStop(ctx->unit);
    AudioUnitUninitialize(ctx->unit);
    AudioComponentInstanceDispose(ctx->unit);
    AudioConverterDispose(ctx->converter);

    delete ctx;
    ctx = nullptr;
}
} // extern "C"