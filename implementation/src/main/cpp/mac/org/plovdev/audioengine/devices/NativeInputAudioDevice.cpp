#include <jni.h>
#include <AudioToolbox/AudioToolbox.h>
#include <CoreFoundation/CoreFoundation.h>
#include <iostream>
#include <cstring>
#include <atomic>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <unordered_map>
#include <memory>
#include <iostream>

#include "h/org_plovdev_audioengine_devices_NativeInputAudioDevice.h"

// Структура данных для конкретного устройства
struct AudioDeviceContext {
    AudioUnit audioUnit;
    AudioStreamBasicDescription format;
    std::atomic<bool> isRunning{false};
    std::vector<uint8_t> buffer;
    std::mutex bufferMutex;
    std::condition_variable dataCondition;
    size_t bytesAvailable{0};
    bool shouldBlock{true};
    bool isInitialized{false};
    std::string deviceId;

    ~AudioDeviceContext() {
        close();
    }

    void close() {
        if (isRunning.load()) {
            isRunning.store(false);
            AudioOutputUnitStop(audioUnit);
        }
        if (isInitialized) {
            AudioUnitUninitialize(audioUnit);
            isInitialized = false;
        }
        if (audioUnit) {
            AudioComponentInstanceDispose(audioUnit);
            audioUnit = nullptr;
        }

        std::lock_guard<std::mutex> lock(bufferMutex);
        buffer.clear();
        buffer.shrink_to_fit();
        bytesAvailable = 0;
        dataCondition.notify_all(); // Разбудить все ждущие потоки
    }
};

Float32 apparatGain = 1;
int applicaionGaine = 1;

std::unordered_map<long, std::unique_ptr<AudioDeviceContext>> inputContexts;
std::mutex inputContextsMutex;
long nextContextId = 1;

AudioDeviceContext* getContext(long handle) {
    std::lock_guard<std::mutex> lock(inputContextsMutex);
    auto it = inputContexts.find(handle);
    return (it != inputContexts.end()) ? it->second.get() : nullptr;
}

// Конвертация Java TrackFormat в AudioStreamBasicDescription
AudioStreamBasicDescription javaToASBD(JNIEnv* env, jobject trackFormat) {
    AudioStreamBasicDescription asbd = {0};

    jclass formatClass = env->GetObjectClass(trackFormat);

    // Получаем поля
    jfieldID sampleRateField = env->GetFieldID(formatClass, "sampleRate", "I");
    jfieldID channelsField = env->GetFieldID(formatClass, "channels", "I");
    jfieldID bitsPerSampleField = env->GetFieldID(formatClass, "bitsPerSample", "I");
    jfieldID signedField = env->GetFieldID(formatClass, "signed", "Z");

    // Получаем ByteOrder
    jfieldID byteOrderField = env->GetFieldID(formatClass, "byteOrder", "Ljava/nio/ByteOrder;");
    jobject byteOrderObj = env->GetObjectField(trackFormat, byteOrderField);
    jclass byteOrderClass = env->FindClass("java/nio/ByteOrder");
    jmethodID toStringMethod = env->GetMethodID(byteOrderClass, "toString", "()Ljava/lang/String;");
    jstring byteOrderStr = (jstring)env->CallObjectMethod(byteOrderObj, toStringMethod);
    const char* byteOrderCStr = env->GetStringUTFChars(byteOrderStr, nullptr);

    bool isBigEndian = (strcmp(byteOrderCStr, "BIG_ENDIAN") == 0);
    env->ReleaseStringUTFChars(byteOrderStr, byteOrderCStr);

    // Получаем значения
    int sampleRate = env->GetIntField(trackFormat, sampleRateField);
    int channels = env->GetIntField(trackFormat, channelsField);
    int bitsPerSample = env->GetIntField(trackFormat, bitsPerSampleField);
    bool isSigned = env->GetBooleanField(trackFormat, signedField);

    // Заполняем структуру
    asbd.mSampleRate = (Float64) sampleRate;
    asbd.mFormatID = kAudioFormatLinearPCM;
    asbd.mFormatFlags = kAudioFormatFlagIsPacked;

    if (isSigned) {
        asbd.mFormatFlags |= kAudioFormatFlagIsSignedInteger;
    }

    if (isBigEndian) {
        asbd.mFormatFlags |= kAudioFormatFlagIsBigEndian;
    }

    asbd.mChannelsPerFrame = channels;
    asbd.mBitsPerChannel = bitsPerSample;
    asbd.mBytesPerFrame = (bitsPerSample / 8) * channels;
    asbd.mFramesPerPacket = 1;
    asbd.mBytesPerPacket = asbd.mBytesPerFrame * asbd.mFramesPerPacket;

    return asbd;
}

// Получение deviceId из AudioDeviceInfo
std::string getDeviceIdFromInfo(JNIEnv* env, jobject deviceInfo) {
    if (!deviceInfo) return "default";

    jclass infoClass = env->GetObjectClass(deviceInfo);
    jfieldID idField = env->GetFieldID(infoClass, "id", "Ljava/lang/String;");
    jstring idStr = (jstring)env->GetObjectField(deviceInfo, idField);

    if (!idStr) return "default";

    const char* idCStr = env->GetStringUTFChars(idStr, nullptr);
    std::string result(idCStr);
    env->ReleaseStringUTFChars(idStr, idCStr);

    return result;
}

// Callback для захвата аудио
OSStatus recordingCallback(void* inRefCon,
                                 AudioUnitRenderActionFlags* ioActionFlags,
                                 const AudioTimeStamp* inTimeStamp,
                                 UInt32 inBusNumber,
                                 UInt32 inNumberFrames,
                                 AudioBufferList* ioData) {

    AudioDeviceContext* context = static_cast<AudioDeviceContext*>(inRefCon);

    if (!context || !context->isRunning) {
        return noErr;
    }

    AudioBufferList bufferList;
    bufferList.mNumberBuffers = 1;
    bufferList.mBuffers[0].mNumberChannels = context->format.mChannelsPerFrame;
    bufferList.mBuffers[0].mDataByteSize = inNumberFrames * context->format.mBytesPerFrame;

    // Временный буфер для данных
    std::vector<uint8_t> tempBuffer(bufferList.mBuffers[0].mDataByteSize);
    bufferList.mBuffers[0].mData = tempBuffer.data();

    // Получаем аудио данные
    OSStatus status = AudioUnitRender(context->audioUnit,
                                     ioActionFlags,
                                     inTimeStamp,
                                     inBusNumber,
                                     inNumberFrames,
                                     &bufferList);

    if (status == noErr && bufferList.mBuffers[0].mDataByteSize > 0) {
        std::lock_guard<std::mutex> lock(context->bufferMutex);

        // Добавляем данные в буфер
        size_t oldSize = context->buffer.size();
        context->buffer.resize(oldSize + tempBuffer.size());
        std::memcpy(context->buffer.data() + oldSize,
                   tempBuffer.data(),
                   tempBuffer.size());

        context->bytesAvailable = context->buffer.size();
        context->dataCondition.notify_one(); // Уведомляем один ждущий поток
    }

    return status;
}


extern "C" {

    /*
     * Class:     org_plovdev_audioengine_devices_NativeInputAudioDevice
     * Method:    _setGain
     * Signature: (II)V
     */
    JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1setGain(JNIEnv* env, jobject obj, jint gain, jint gainType, jlong handle) {
        switch(gainType) {
            case 0:
                apparatGain = static_cast<Float32>(gain);
                break;
            case 1:
                applicaionGaine = static_cast<int>(gain);
                break;
        }
    }

    /*
     * Class:     org_plovdev_audioengine_devices_NativeInputAudioDevice
     * Method:    _open
     * Signature: (Lorg/plovdev/audioengine/tracks/format/TrackFormat;Lorg/plovdev/audioengine/devices/AudioDeviceInfo;)J
     */
    JNIEXPORT jlong JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1open(JNIEnv* env, jobject obj, jobject trackFormat, jobject deviceInfo) {
        auto deviceContext = std::make_unique<AudioDeviceContext>();
        AudioDeviceContext* deviceContextPtr = deviceContext.get();

        try {
            // Получаем deviceId из deviceInfo
            deviceContext->deviceId = getDeviceIdFromInfo(env, deviceInfo);

            // Конвертируем формат
            deviceContext->format = javaToASBD(env, trackFormat);

            // Настраиваем AudioComponent
            AudioComponentDescription desc{};
            desc.componentType = kAudioUnitType_Output;
            desc.componentSubType = kAudioUnitSubType_HALOutput;
            desc.componentManufacturer = kAudioUnitManufacturer_Apple;

            AudioComponent comp = AudioComponentFindNext(nullptr, &desc);
            if (!comp) throw std::runtime_error("No audio input device found");

            OSStatus status = AudioComponentInstanceNew(comp, &deviceContext->audioUnit);
            if (status != noErr) throw std::runtime_error("Failed to create AudioUnit");

            // Включаем Input, выключаем Output
            UInt32 enableInput = 1;
            UInt32 disableOutput = 0;
            status = AudioUnitSetProperty(deviceContext->audioUnit,
                                          kAudioOutputUnitProperty_EnableIO,
                                          kAudioUnitScope_Input, 1, &enableInput, sizeof(enableInput));
            if (status != noErr) throw std::runtime_error("Failed to enable input");

            status = AudioUnitSetProperty(deviceContext->audioUnit,
                                          kAudioOutputUnitProperty_EnableIO,
                                          kAudioUnitScope_Output, 0, &disableOutput, sizeof(disableOutput));
            if (status != noErr) throw std::runtime_error("Failed to disable output");

            // Преобразуем deviceId строки в AudioDeviceID
            AudioDeviceID inputDevice = kAudioDeviceUnknown;
            if (!deviceContext->deviceId.empty()) {
                inputDevice = static_cast<AudioDeviceID>(strtoul(deviceContext->deviceId.c_str(), nullptr, 10));
            }

            if (inputDevice == kAudioDeviceUnknown) {
                throw std::runtime_error("Invalid deviceId");
            }

            // Устанавливаем выбранное устройство
            status = AudioUnitSetProperty(deviceContext->audioUnit,
                                          kAudioOutputUnitProperty_CurrentDevice,
                                          kAudioUnitScope_Global,
                                          0,
                                          &inputDevice,
                                          sizeof(inputDevice));
            if (status != noErr) throw std::runtime_error("Failed to set input device");

            // Формат для Input bus
            status = AudioUnitSetProperty(deviceContext->audioUnit,
                                          kAudioUnitProperty_StreamFormat,
                                          kAudioUnitScope_Output,
                                          1,
                                          &deviceContext->format,
                                          sizeof(deviceContext->format));
            if (status != noErr) throw std::runtime_error("Failed to set stream format");

            // Callback
            AURenderCallbackStruct callbackStruct{};
            callbackStruct.inputProc = recordingCallback;
            callbackStruct.inputProcRefCon = deviceContext.get();

            status = AudioUnitSetProperty(deviceContext->audioUnit,
                                          kAudioOutputUnitProperty_SetInputCallback,
                                          kAudioUnitScope_Global,
                                          1,
                                          &callbackStruct,
                                          sizeof(callbackStruct));
            if (status != noErr) throw std::runtime_error("Failed to set callback");

            // Инициализация
            status = AudioUnitInitialize(deviceContext->audioUnit);
            if (status != noErr) throw std::runtime_error("Failed to initialize AudioUnit");

            deviceContext->isInitialized = true;

            // Буфер на 2 секунды
            size_t bufferSize = deviceContext->format.mSampleRate *
                                deviceContext->format.mBytesPerFrame * 2;
            deviceContext->buffer.reserve(bufferSize);
            deviceContext->bytesAvailable = 0;
            deviceContext->isRunning = false;

        } catch (const std::exception& e) {
            if (deviceContext) {
                deviceContext->close();
                deviceContext.reset();
            }
            jclass exClass = env->FindClass("org/plovdev/audioengine/exceptions/OpenAudioDeviceException");
            env->ThrowNew(exClass, e.what());
        }

        std::lock_guard<std::mutex> lock(inputContextsMutex);
        long handle = nextContextId++;
        inputContexts[handle] = std::move(deviceContext);
        return handle;
    }


    /*
     * Class:     org_plovdev_audioengine_devices_NativeInputAudioDevice
     * Method:    _read
     * Signature: (Ljava/nio/ByteBuffer;J)I
     */
    JNIEXPORT jint JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1read(JNIEnv* env, jobject obj, jobject byteBuffer, jlong handle) {
        auto* deviceContext = getContext(handle);

        if (!deviceContext) {
            return 0;
        }

        void* bufferPtr = env->GetDirectBufferAddress(byteBuffer);
        jlong capacity = env->GetDirectBufferCapacity(byteBuffer);

        if (!bufferPtr || capacity <= 0) {
            return -1;
        }

        // Запускаем запись если еще не запущена
        if (!deviceContext->isRunning) {
            OSStatus status = AudioOutputUnitStart(deviceContext->audioUnit);
            if (status != noErr) {
                return -1;
            }
            deviceContext->isRunning = true;
        }

        lock.unlock(); // Отпускаем глобальную блокировку перед ожиданием данных

        uint8_t* dest = static_cast<uint8_t*>(bufferPtr);
        size_t bytesCopied = 0;
        size_t targetBytes = static_cast<size_t>(capacity);

        // Ждем и копируем пока не заполним весь буфер
        while (bytesCopied < targetBytes) {
            std::unique_lock<std::mutex> bufferLock(deviceContext->bufferMutex);

            // Ждем данных, пока не наберется достаточно или не будет сигнала остановки
            deviceContext->dataCondition.wait(bufferLock, [&]() {
                return deviceContext->bytesAvailable > 0 || !deviceContext->isRunning;
            });

            if (!deviceContext->isRunning) {
                // Запись остановлена
                break;
            }

            // Копируем сколько можем
            size_t bytesToCopy = std::min(
                deviceContext->bytesAvailable,
                targetBytes - bytesCopied
            );

            if (bytesToCopy > 0) {
                std::memcpy(dest + bytesCopied,
                           deviceContext->buffer.data(),
                           bytesToCopy);
                bytesCopied += bytesToCopy;

                // Удаляем скопированные данные
                if (bytesToCopy < deviceContext->buffer.size()) {
                    std::memmove(deviceContext->buffer.data(),
                                deviceContext->buffer.data() + bytesToCopy,
                                deviceContext->buffer.size() - bytesToCopy);
                    deviceContext->buffer.resize(deviceContext->buffer.size() - bytesToCopy);
                } else {
                    deviceContext->buffer.clear();
                }

                deviceContext->bytesAvailable = deviceContext->buffer.size();
            }

            bufferLock.unlock();

            // Если скопировали все, выходим
            if (bytesCopied >= targetBytes) {
                break;
            }
        }

        return static_cast<jint>(bytesCopied);
    }

    /*
     * Class:     org_plovdev_audioengine_devices_NativeInputAudioDevice
     * Method:    _close
     * Signature: (J)V
     */
    JNIEXPORT void JNICALL Java_org_plovdev_audioengine_devices_NativeInputAudioDevice__1close(JNIEnv* env, jobject obj, jlong handle) {
        auto* ctx = getContext(handle);
        if (!ctx) return;

        ctx->close();

        std::lock_guard<std::mutex> lock(inputContextsMutex);
        inputContexts.erase(handle);
    }
}