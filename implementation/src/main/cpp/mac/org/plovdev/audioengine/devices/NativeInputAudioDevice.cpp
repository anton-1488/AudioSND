#include <jni.h>
#include <AudioToolbox/AudioToolbox.h>
#include <CoreFoundation/CoreFoundation.h>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include <algorithm>
#include <unordered_map>


// Структура кольцевого буфера, чтобы избежать memmove и аллокаций в реалтайме
struct RingBuffer {
    std::vector<uint8_t> data;
    size_t head = 0;
    size_t tail = 0;
    std::atomic<size_t> size{0};
    size_t capacity = 0;

    void init(size_t cap) {
        data.assign(cap, 0);
        capacity = cap;
        head = tail = 0;
        size = 0;
    }

    void write(const uint8_t* src, size_t len) {
        if (len > capacity - size) len = capacity - size; // Защита от переполнения
        if (len == 0) return;

        size_t firstPart = std::min(len, capacity - head);
        std::memcpy(&data[head], src, firstPart);

        if (len > firstPart) {
            std::memcpy(&data[0], src + firstPart, len - firstPart);
        }

        head = (head + len) % capacity;
        size += len;
    }

    size_t read(uint8_t* dest, size_t len) {
        size_t canRead = std::min(len, (size_t)size);
        if (canRead == 0) return 0;

        size_t firstPart = std::min(canRead, capacity - tail);
        std::memcpy(dest, &data[tail], firstPart);

        if (canRead > firstPart) {
            std::memcpy(dest + firstPart, &data[0], canRead - firstPart);
        }

        tail = (tail + canRead) % capacity;
        size -= canRead;
        return canRead;
    }
};


struct AudioDeviceContext {
    AudioUnit audioUnit = nullptr;
    AudioStreamBasicDescription format;
    std::atomic<bool> isRunning{false};
    bool isInitialized{false};

    RingBuffer ringBuffer;
    std::mutex bufferMutex;
    std::condition_variable dataCondition;

    // Временный буфер для рендеринга (выделяется один раз при open)
    std::vector<uint8_t> renderBuffer;

    void close() {
        isRunning.store(false);
        dataCondition.notify_all();

        if (audioUnit) {
            AudioOutputUnitStop(audioUnit);
            AudioUnitUninitialize(audioUnit);
            AudioComponentInstanceDispose(audioUnit);
            audioUnit = nullptr;
        }
        isInitialized = false;
    }
};

// Глобальные хранилища
std::unordered_map<long, std::unique_ptr<AudioDeviceContext>> inputContexts;
std::mutex inputContextsMutex;
long nextInputContextId = 1;

AudioDeviceContext* getInputContext(long handle) {
    std::lock_guard<std::mutex> lock(inputContextsMutex);
    auto it = inputContexts.find(handle);
    return (it != inputContexts.end()) ? it->second.get() : nullptr;
}

// --- JNI Вспомогательные функции ---

AudioStreamBasicDescription javaToASBD(JNIEnv* env, jobject trackFormat) {
    AudioStreamBasicDescription asbd = {0};
    jclass cls = env->GetObjectClass(trackFormat);

    asbd.mSampleRate = (Float64)env->GetIntField(trackFormat, env->GetFieldID(cls, "sampleRate", "I"));
    int channels = env->GetIntField(trackFormat, env->GetFieldID(cls, "channels", "I"));
    int bits = env->GetIntField(trackFormat, env->GetFieldID(cls, "bitDepth", "I"));

    asbd.mFormatID = kAudioFormatLinearPCM;
    asbd.mFormatFlags = kAudioFormatFlagIsPacked | kAudioFormatFlagIsSignedInteger;

    // Проверка порядка байт (упрощено)
    jfieldID boField = env->GetFieldID(cls, "byteOrder", "Ljava/nio/ByteOrder;");
    jobject boObj = env->GetObjectField(trackFormat, boField);
    // Для macOS обычно Little Endian, если не указано иное

    asbd.mChannelsPerFrame = channels;
    asbd.mBitsPerChannel = bits;
    asbd.mBytesPerFrame = (bits / 8) * channels;
    asbd.mFramesPerPacket = 1;
    asbd.mBytesPerPacket = asbd.mBytesPerFrame;
    return asbd;
}

// --- Callback (Real-time thread) ---

OSStatus recordingCallback(void* inRefCon, AudioUnitRenderActionFlags* ioActionFlags,
                           const AudioTimeStamp* inTimeStamp, UInt32 inBusNumber,
                           UInt32 inNumberFrames, AudioBufferList* ioData) {

    auto* ctx = static_cast<AudioDeviceContext*>(inRefCon);
    if (!ctx || !ctx->isRunning.load()) return noErr;

    AudioBufferList bufferList;
    bufferList.mNumberBuffers = 1;
    bufferList.mBuffers[0].mNumberChannels = ctx->format.mChannelsPerFrame;
    bufferList.mBuffers[0].mDataByteSize = inNumberFrames * ctx->format.mBytesPerFrame;
    bufferList.mBuffers[0].mData = ctx->renderBuffer.data();

    // Рендерим звук из железа в renderBuffer
    OSStatus status = AudioUnitRender(ctx->audioUnit, ioActionFlags, inTimeStamp, inBusNumber, inNumberFrames, &bufferList);

    if (status == noErr) {
        // Захватываем мьютекс только на время записи в кольцо
        std::lock_guard<std::mutex> lock(ctx->bufferMutex);
        ctx->ringBuffer.write((uint8_t*)bufferList.mBuffers[0].mData, bufferList.mBuffers[0].mDataByteSize);
        ctx->dataCondition.notify_one();
    }
    return noErr;
}


extern "C" {

JNIEXPORT jlong JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1open(JNIEnv* env, jobject obj, jobject trackFormat, jobject deviceInfo) {
    auto ctx = std::make_unique<AudioDeviceContext>();

    try {
        ctx->format = javaToASBD(env, trackFormat);

        // 1. Создаем AUHAL (Audio Unit Hardware Abstraction Layer)
        AudioComponentDescription desc{ kAudioUnitType_Output, kAudioUnitSubType_HALOutput, kAudioUnitManufacturer_Apple, 0, 0 };
        AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
        AudioComponentInstanceNew(comp, &ctx->audioUnit);

        // 2. Включаем вход (Input Scope на Bus 1)
        UInt32 enable = 1;
        AudioUnitSetProperty(ctx->audioUnit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Input, 1, &enable, sizeof(enable));

        // 3. Выключаем выход (Output Scope на Bus 0) - ОБЯЗАТЕЛЬНО
        UInt32 disable = 0;
        AudioUnitSetProperty(ctx->audioUnit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Output, 0, &disable, sizeof(disable));

        // 4. Получаем реальный девайс (Default Input)
        AudioDeviceID devId;
        UInt32 devIdSize = sizeof(devId);
        AudioObjectPropertyAddress addr = { kAudioHardwarePropertyDefaultInputDevice, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster };
        AudioObjectGetPropertyData(kAudioObjectSystemObject, &addr, 0, nullptr, &devIdSize, &devId);
        AudioUnitSetProperty(ctx->audioUnit, kAudioOutputUnitProperty_CurrentDevice, kAudioUnitScope_Global, 0, &devId, sizeof(devId));

        // 5. Узнаем нативный формат микрофона (чтобы не было конфликта)
        AudioStreamBasicDescription deviceFormat;
        UInt32 asbdSize = sizeof(deviceFormat);
        AudioUnitGetProperty(ctx->audioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Input, 1, &deviceFormat, &asbdSize);

        // ПРИНУДИТЕЛЬНО ставим формат из Java на ВЫХОД входной шины
        // Именно здесь Apple включает свой встроенный конвертер и ресемплер
        OSStatus status = AudioUnitSetProperty(ctx->audioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Output, 1, &ctx->format, sizeof(ctx->format));
        if (status != noErr) throw std::runtime_error("Format not supported");

        // 6. Устанавливаем размер буфера (чтобы не было "микрофрагментов")
        UInt32 maxFrames = 4096;
        AudioUnitSetProperty(ctx->audioUnit, kAudioUnitProperty_MaximumFramesPerSlice, kAudioUnitScope_Global, 0, &maxFrames, sizeof(maxFrames));

        // 7. Callback
        AURenderCallbackStruct cb{ recordingCallback, ctx.get() };
        AudioUnitSetProperty(ctx->audioUnit, kAudioOutputUnitProperty_SetInputCallback, kAudioUnitScope_Global, 1, &cb, sizeof(cb));

        // 8. Инициализация буферов (БЕРЕМ С ЗАПАСОМ)
        ctx->ringBuffer.init(ctx->format.mSampleRate * ctx->format.mBytesPerFrame * 5); // 5 секунд
        ctx->renderBuffer.resize(maxFrames * ctx->format.mBytesPerFrame);

        AudioUnitInitialize(ctx->audioUnit);
        ctx->isInitialized = true;

    } catch (const std::exception& e) {
        return -1;
    }


    std::lock_guard<std::mutex> lock(inputContextsMutex);
    long handle = nextInputContextId++;
    inputContexts[handle] = std::move(ctx);
    return handle;
}

JNIEXPORT jint JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1read(JNIEnv* env, jobject obj, jobject byteBuffer, jlong handle) {
    auto* ctx = getInputContext(handle);
    if (!ctx) return -1;

    uint8_t* dest = (uint8_t*)env->GetDirectBufferAddress(byteBuffer);
    size_t capacity = (size_t)env->GetDirectBufferCapacity(byteBuffer);

    if (!ctx->isRunning) {
        AudioOutputUnitStart(ctx->audioUnit);
        ctx->isRunning = true;
    }

    size_t totalRead = 0;
    while (totalRead < capacity) {
        std::unique_lock<std::mutex> lock(ctx->bufferMutex);
        // Ждем, пока в кольцевом буфере появится хоть что-то
        ctx->dataCondition.wait(lock, [&]{ return ctx->ringBuffer.size > 0 || !ctx->isRunning; });

        if (!ctx->isRunning) break;

        size_t readNow = ctx->ringBuffer.read(dest + totalRead, capacity - totalRead);
        totalRead += readNow;

        if (totalRead >= capacity) break;
    }

    return (jint)totalRead;
}

JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1close(JNIEnv* env, jobject obj, jlong handle) {
    std::lock_guard<std::mutex> lock(inputContextsMutex);
    auto it = inputContexts.find(handle);
    if (it != inputContexts.end()) {
        it->second->close();
        inputContexts.erase(it);
    }
}

} // extern "C"