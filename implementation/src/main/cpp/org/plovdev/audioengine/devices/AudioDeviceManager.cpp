#include <iostream>
#include <jni.h>
#include <CoreAudio/CoreAudio.h>
#include <vector>
#include <string>

#include "org_plovdev_audioengine_devices_AudioDeviceManager.h"

// =====================
// Utils
// =====================
std::string CFStringToStdString(CFStringRef cfStr) {
    if (!cfStr) return "";
    char buffer[512];
    if (CFStringGetCString(cfStr, buffer, sizeof(buffer), kCFStringEncodingUTF8)) {
        return buffer;
    }
    return "";
}

// =====================
// JNI cache
// =====================
jclass clsAudioDeviceInfo = nullptr;
jmethodID ctorAudioDeviceInfo = nullptr;

jclass clsInputDevice = nullptr;
jmethodID ctorInputDevice = nullptr;

jclass clsOutputDevice = nullptr;  // Добавлено для выходных устройств
jmethodID ctorOutputDevice = nullptr;

jclass clsArrayList = nullptr;
jmethodID ctorArrayList = nullptr;
jmethodID arrayListAdd = nullptr;

jclass clsTrackFormat = nullptr;
jmethodID ctorTrackFormat = nullptr;

// скелеты функций
jobject getDeviceSupportedFormats(JNIEnv* env, AudioDeviceID devId, int channels, bool isInput);

// =====================
// Init JNI cache
// =====================
bool initCommonJNI(JNIEnv* env) {
    if (clsAudioDeviceInfo) return true;

    clsAudioDeviceInfo = (jclass) env->NewGlobalRef(
        env->FindClass("org/plovdev/audioengine/devices/AudioDeviceInfo")
    );
    if (!clsAudioDeviceInfo) return false;

    ctorAudioDeviceInfo = env->GetMethodID(
        clsAudioDeviceInfo,
        "<init>",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/Set;)V"
    );
    if (!ctorAudioDeviceInfo) return false;

    clsArrayList = (jclass) env->NewGlobalRef(
        env->FindClass("java/util/ArrayList")
    );
    ctorArrayList = env->GetMethodID(clsArrayList, "<init>", "()V");
    arrayListAdd = env->GetMethodID(clsArrayList, "add", "(Ljava/lang/Object;)Z");

    return ctorArrayList && arrayListAdd;
}

bool initJNIForInput(JNIEnv* env) {
    if (clsInputDevice) return true;

    clsInputDevice = (jclass) env->NewGlobalRef(
        env->FindClass("org/plovdev/audioengine/devices/NativeInputAudioDevice")
    );
    if (!clsInputDevice) return false;

    ctorInputDevice = env->GetMethodID(
        clsInputDevice,
        "<init>",
        "(Lorg/plovdev/audioengine/devices/AudioDeviceInfo;)V"
    );
    return ctorInputDevice != nullptr;
}

bool initJNIForOutput(JNIEnv* env) {
    if (clsOutputDevice) return true;

    clsOutputDevice = (jclass) env->NewGlobalRef(
        env->FindClass("org/plovdev/audioengine/devices/NativeOutputAudioDevice")
    );
    if (!clsOutputDevice) return false;

    ctorOutputDevice = env->GetMethodID(
        clsOutputDevice,
        "<init>",
        "(Lorg/plovdev/audioengine/devices/AudioDeviceInfo;)V"
    );
    return ctorOutputDevice != nullptr;
}

// =====================
// Create AudioDeviceInfo
// =====================
jobject createAudioDeviceInfo(
        JNIEnv* env,
        AudioDeviceID devId,
        const std::string& name,
        int channels,
        bool isInput
) {
    jclass hashSetCls = env->FindClass("java/util/HashSet");
    jmethodID hashSetCtor = env->GetMethodID(hashSetCls, "<init>", "()V");
    jobject emptySet = env->NewObject(hashSetCls, hashSetCtor);

    jclass clsInteger = env->FindClass("java/lang/Integer");
    jmethodID ctorInteger = env->GetMethodID(clsInteger, "<init>", "(I)V");
    jobject jintObj = env->NewObject(clsInteger, ctorInteger, channels);

    const char* vendorStr = "Apple";
    UInt32 vendor = 0;
    UInt32 sizeVendor = sizeof(vendor);
    AudioObjectPropertyAddress propAddrVendor{
        kAudioDevicePropertyDeviceManufacturer,
        kAudioObjectPropertyScopeGlobal,
        kAudioObjectPropertyElementMaster
    };

    if (AudioObjectGetPropertyData(devId, &propAddrVendor, 0, nullptr, &sizeVendor, &vendor) == noErr) {
        char buf[5];
        buf[0] = (vendor >> 24) & 0xFF;
        buf[1] = (vendor >> 16) & 0xFF;
        buf[2] = (vendor >> 8) & 0xFF;
        buf[3] = (vendor) & 0xFF;
        buf[4] = '\0';
        vendorStr = buf;
    }

    return env->NewObject(
        clsAudioDeviceInfo,
        ctorAudioDeviceInfo,
        env->NewStringUTF(std::to_string(devId).c_str()),
        env->NewStringUTF(name.c_str()),
        env->NewStringUTF(vendorStr),
        jintObj,
        getDeviceSupportedFormats(env, devId, channels, isInput)
    );
}

jobject getAudioCodecForASBD(JNIEnv* env, const AudioStreamBasicDescription& asbd) {
    jclass clsAudioCodec = env->FindClass("org/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec");
    if (!clsAudioCodec) return nullptr;

    bool isFloat  = (asbd.mFormatFlags & kAudioFormatFlagIsFloat) != 0;
    bool isSigned = (asbd.mFormatFlags & kAudioFormatFlagIsSignedInteger) != 0;

    jfieldID fidCodec = nullptr;

    if (isFloat) {
        if (asbd.mBitsPerChannel == 32) {
            fidCodec = env->GetStaticFieldID(clsAudioCodec, "FLOAT32", "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;");
        } else if (asbd.mBitsPerChannel == 64) {
            fidCodec = env->GetStaticFieldID(clsAudioCodec, "FLOAT64", "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;");
        }
    } else if (isSigned) {
        switch(asbd.mBitsPerChannel) {
            case 8:  fidCodec = env->GetStaticFieldID(clsAudioCodec, "PCM8",  "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;"); break;
            case 16: fidCodec = env->GetStaticFieldID(clsAudioCodec, "PCM16", "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;"); break;
            case 24: fidCodec = env->GetStaticFieldID(clsAudioCodec, "PCM24", "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;"); break;
            case 32: fidCodec = env->GetStaticFieldID(clsAudioCodec, "PCM32", "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;"); break;
        }
    } else {
        fidCodec = env->GetStaticFieldID(clsAudioCodec, "PCM16", "Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;");
    }

    if (!fidCodec) return nullptr;

    return env->GetStaticObjectField(clsAudioCodec, fidCodec);
}

jobject getDeviceSupportedFormats(JNIEnv* env, AudioDeviceID devId, int channels, bool isInput) {
    jclass hashSetCls = env->FindClass("java/util/HashSet");
    jmethodID hashSetCtor = env->GetMethodID(hashSetCls, "<init>", "()V");
    jmethodID hashSetAdd = env->GetMethodID(hashSetCls, "add", "(Ljava/lang/Object;)Z");
    jobject set = env->NewObject(hashSetCls, hashSetCtor);

    jclass clsByteOrder = env->FindClass("java/nio/ByteOrder");
    jfieldID fidLE = env->GetStaticFieldID(clsByteOrder, "LITTLE_ENDIAN", "Ljava/nio/ByteOrder;");
    jobject byteOrderLE = env->GetStaticObjectField(clsByteOrder, fidLE);

    // Инициализируем clsTrackFormat если еще не инициализирован
    if (!clsTrackFormat) {
        clsTrackFormat = (jclass) env->NewGlobalRef(env->FindClass("org/plovdev/audioengine/tracks/format/TrackFormat"));
        ctorTrackFormat = env->GetMethodID(
            clsTrackFormat,
            "<init>",
            "(Ljava/lang/String;IIIZLjava/nio/ByteOrder;Lorg/plovdev/audioengine/tracks/format/TrackFormat$AudioCodec;)V"
        );
    }

    if (!clsTrackFormat || !ctorTrackFormat) {
        return set;
    }

    // Используем правильный scope в зависимости от типа устройства
    AudioObjectPropertyScope scope = isInput ? kAudioObjectPropertyScopeInput : kAudioObjectPropertyScopeOutput;

    AudioObjectPropertyAddress addrStreams{
        kAudioDevicePropertyStreams,
        scope,
        kAudioObjectPropertyElementMaster
    };

    UInt32 size = 0;
    if (AudioObjectGetPropertyDataSize(devId, &addrStreams, 0, nullptr, &size) != noErr) {
        return set;
    }

    UInt32 streamCount = size / sizeof(AudioStreamID);
    std::vector<AudioStreamID> streams(streamCount);
    if (AudioObjectGetPropertyData(devId, &addrStreams, 0, nullptr, &size, streams.data()) != noErr) {
        return set;
    }

    for (UInt32 i = 0; i < streamCount; i++) {
        AudioStreamID streamID = streams[i];

        UInt32 availableFormatsSize = 0;
        AudioObjectPropertyAddress addrFormats{
            kAudioStreamPropertyPhysicalFormats,
            scope,
            kAudioObjectPropertyElementMaster
        };

        if (AudioObjectGetPropertyDataSize(streamID, &addrFormats, 0, nullptr, &availableFormatsSize) != noErr) {
            continue;
        }

        UInt32 formatCount = availableFormatsSize / sizeof(AudioStreamBasicDescription);
        std::vector<AudioStreamBasicDescription> availableFormats(formatCount);

        if (AudioObjectGetPropertyData(streamID, &addrFormats, 0, nullptr, &availableFormatsSize, availableFormats.data()) != noErr) {
            continue;
        }

        for (const auto& asbd : availableFormats) {
            if ((int)asbd.mChannelsPerFrame != channels) continue;

            bool isSigned = (asbd.mFormatFlags & kAudioFormatFlagIsSignedInteger) != 0;
            bool isFloat  = (asbd.mFormatFlags & kAudioFormatFlagIsFloat) != 0;
            jboolean signedFlag = isSigned || isFloat;

            jobject codecEnum = getAudioCodecForASBD(env, asbd);

            jobject tf = env->NewObject(
                clsTrackFormat, ctorTrackFormat,
                env->NewStringUTF("wav"),
                (jint)asbd.mChannelsPerFrame,
                (jint)asbd.mBitsPerChannel,
                (jint)asbd.mSampleRate,
                signedFlag,
                byteOrderLE,
                codecEnum
            );

            if (tf) env->CallBooleanMethod(set, hashSetAdd, tf);
        }
    }

    return set;
}

// =====================
// JNI entry
// =====================
extern "C" {
    JNIEXPORT jobject JNICALL Java_org_plovdev_audioengine_devices_AudioDeviceManager_getInputDevices(JNIEnv* env, jobject jobj) {
        if (!initCommonJNI(env) || !initJNIForInput(env)) {
            return nullptr;
        }

        jobject list = env->NewObject(clsArrayList, ctorArrayList);

        UInt32 size = 0;
        AudioObjectPropertyAddress addr{
            kAudioHardwarePropertyDevices,
            kAudioObjectPropertyScopeInput,
            kAudioObjectPropertyElementMaster
        };

        if (AudioObjectGetPropertyDataSize(
                kAudioObjectSystemObject,
                &addr,
                0,
                nullptr,
                &size
        ) != noErr) {
            return list;
        }

        int count = size / sizeof(AudioDeviceID);
        std::vector<AudioDeviceID> devices(count);

        if (AudioObjectGetPropertyData(
                kAudioObjectSystemObject,
                &addr,
                0,
                nullptr,
                &size,
                devices.data()
        ) != noErr) {
            return list;
        }

        for (AudioDeviceID devId : devices) {
            // Проверяем что это входное устройство (микрофон)
            AudioObjectPropertyAddress streamAddr{
                kAudioDevicePropertyStreamConfiguration,
                kAudioObjectPropertyScopeInput,
                kAudioObjectPropertyElementMaster
            };

            UInt32 streamSize = 0;
            if (AudioObjectGetPropertyDataSize(devId, &streamAddr, 0, nullptr, &streamSize) != noErr || streamSize == 0) {
                continue;
            }

            AudioBufferList* bufferList = (AudioBufferList*)malloc(streamSize);
            if (!bufferList) continue;

            if (AudioObjectGetPropertyData(devId, &streamAddr, 0, nullptr, &streamSize, bufferList) != noErr) {
                free(bufferList);
                continue;
            }

            int inputChannels = 0;
            for (UInt32 i = 0; i < bufferList->mNumberBuffers; i++) {
                inputChannels += bufferList->mBuffers[i].mNumberChannels;
            }
            free(bufferList);

            if (inputChannels == 0) continue;

            // Получаем имя устройства
            CFStringRef nameRef = nullptr;
            UInt32 propSize = sizeof(nameRef);

            AudioObjectPropertyAddress nameAddr{
                kAudioObjectPropertyName,
                kAudioObjectPropertyScopeInput,
                kAudioObjectPropertyElementMaster
            };

            if (AudioObjectGetPropertyData(
                    devId,
                    &nameAddr,
                    0,
                    nullptr,
                    &propSize,
                    &nameRef
            ) != noErr || !nameRef) {
                continue;
            }

            std::string name = CFStringToStdString(nameRef);
            CFRelease(nameRef);

            jobject info = createAudioDeviceInfo(env, devId, name, inputChannels, true);
            if (!info) continue;

            jobject device = env->NewObject(clsInputDevice, ctorInputDevice, info);
            if (!device) continue;

            env->CallBooleanMethod(list, arrayListAdd, device);
        }

        return list;
    }

    JNIEXPORT jobject JNICALL Java_org_plovdev_audioengine_devices_AudioDeviceManager_getOutputDevices(JNIEnv* env, jobject jobj) {
        if (!initCommonJNI(env) || !initJNIForOutput(env)) {
            return nullptr;
        }

        jobject list = env->NewObject(clsArrayList, ctorArrayList);

        UInt32 size = 0;
        AudioObjectPropertyAddress addr{
            kAudioHardwarePropertyDevices,
            kAudioObjectPropertyScopeOutput,  // Исправлено: Scope Output
            kAudioObjectPropertyElementMaster
        };

        if (AudioObjectGetPropertyDataSize(
                kAudioObjectSystemObject,
                &addr,
                0,
                nullptr,
                &size
        ) != noErr) {
            return list;
        }

        int count = size / sizeof(AudioDeviceID);
        std::vector<AudioDeviceID> devices(count);

        if (AudioObjectGetPropertyData(
                kAudioObjectSystemObject,
                &addr,
                0,
                nullptr,
                &size,
                devices.data()
        ) != noErr) {
            return list;
        }

        for (AudioDeviceID devId : devices) {
            // Проверяем что это выходное устройство (динамики)
            AudioObjectPropertyAddress streamAddr{
                kAudioDevicePropertyStreamConfiguration,
                kAudioObjectPropertyScopeOutput,
                kAudioObjectPropertyElementMaster
            };

            UInt32 streamSize = 0;
            if (AudioObjectGetPropertyDataSize(devId, &streamAddr, 0, nullptr, &streamSize) != noErr || streamSize == 0) {
                continue;
            }

            AudioBufferList* bufferList = (AudioBufferList*)malloc(streamSize);
            if (!bufferList) continue;

            if (AudioObjectGetPropertyData(devId, &streamAddr, 0, nullptr, &streamSize, bufferList) != noErr) {
                free(bufferList);
                continue;
            }

            int outputChannels = 0;
            for (UInt32 i = 0; i < bufferList->mNumberBuffers; i++) {
                outputChannels += bufferList->mBuffers[i].mNumberChannels;
            }
            free(bufferList);

            if (outputChannels == 0) continue;

            // Получаем имя устройства
            CFStringRef nameRef = nullptr;
            UInt32 propSize = sizeof(nameRef);

            AudioObjectPropertyAddress nameAddr{
                kAudioObjectPropertyName,
                kAudioObjectPropertyScopeOutput,  // Исправлено: Scope Output
                kAudioObjectPropertyElementMaster
            };

            if (AudioObjectGetPropertyData(
                    devId,
                    &nameAddr,
                    0,
                    nullptr,
                    &propSize,
                    &nameRef
            ) != noErr || !nameRef) {
                continue;
            }

            std::string name = CFStringToStdString(nameRef);
            CFRelease(nameRef);

            jobject info = createAudioDeviceInfo(env, devId, name, outputChannels, false);
            if (!info) continue;

            // Исправлено: создаем NativeOutputAudioDevice вместо NativeInputAudioDevice
            jobject device = env->NewObject(clsOutputDevice, ctorOutputDevice, info);
            if (!device) continue;

            env->CallBooleanMethod(list, arrayListAdd, device);
        }

        return list;
    }
}