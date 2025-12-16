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

jclass clsArrayList = nullptr;
jmethodID ctorArrayList = nullptr;
jmethodID arrayListAdd = nullptr;

jclass clsTrackFormat = nullptr;
jmethodID ctorTrackFormat = nullptr;

// скелеты функций
jobject getDeviceSupportedFormats(JNIEnv* env, AudioDeviceID devId, int channels);

// =====================
// Init JNI cache
// =====================
bool initJNI(JNIEnv* env, const char* devicePath) {
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



    clsInputDevice = (jclass) env->NewGlobalRef(
        env->FindClass(devicePath)
    );
    if (!clsInputDevice) return false;


    ctorInputDevice = env->GetMethodID(
        clsInputDevice,
        "<init>",
        "(Lorg/plovdev/audioengine/devices/AudioDeviceInfo;)V"
    );
    if (!ctorInputDevice) return false;


    clsArrayList = (jclass) env->NewGlobalRef(
        env->FindClass("java/util/ArrayList")
    );
    ctorArrayList = env->GetMethodID(clsArrayList, "<init>", "()V");
    arrayListAdd = env->GetMethodID(clsArrayList, "add", "(Ljava/lang/Object;)Z");

    return ctorArrayList && arrayListAdd;
}

// =====================
// Create AudioDeviceInfo
// =====================
jobject createAudioDeviceInfo(
        JNIEnv* env,
        AudioDeviceID devId,
        const std::string& name,
        int channels
) {
    jclass hashSetCls = env->FindClass("java/util/HashSet");
        jmethodID hashSetCtor = env->GetMethodID(hashSetCls, "<init>", "()V");
        jobject emptySet = env->NewObject(hashSetCls, hashSetCtor);

    jclass clsInteger = env->FindClass("java/lang/Integer");
    jmethodID ctorInteger = env->GetMethodID(clsInteger, "<init>", "(I)V");
    jobject jintObj = env->NewObject(clsInteger, ctorInteger, channels); // channels — обычный int


    const char* vendorStr = "Apple";
    UInt32 vendor = 0;
    UInt32 sizeVendor = sizeof(vendor);
    AudioObjectPropertyAddress propAddrVendor{
        kAudioDevicePropertyDeviceManufacturer,
        kAudioObjectPropertyScopeGlobal,
        kAudioObjectPropertyElementMaster
    };

    if (AudioObjectGetPropertyData(devId, &propAddrVendor, 0, nullptr, &sizeVendor, &vendor) == noErr) {
        // конвертируем FourCC в строку
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
        getDeviceSupportedFormats(env, devId, channels)
    );
}


jobject getDeviceSupportedFormats(JNIEnv* env, AudioDeviceID devId, int channels) {
    // Создаём HashSet и получаем ссылки JNI (этот блок остается прежним)
    jclass hashSetCls = env->FindClass("java/util/HashSet");
    jmethodID hashSetCtor = env->GetMethodID(hashSetCls, "<init>", "()V");
    jmethodID hashSetAdd = env->GetMethodID(hashSetCls, "add", "(Ljava/lang/Object;)Z");
    jobject set = env->NewObject(hashSetCls, hashSetCtor);

    jclass clsByteOrder = env->FindClass("java/nio/ByteOrder");
    jfieldID fidLE = env->GetStaticFieldID(clsByteOrder, "LITTLE_ENDIAN", "Ljava/nio/ByteOrder;");
    jobject byteOrderLE = env->GetStaticObjectField(clsByteOrder, fidLE);

    clsTrackFormat = (jclass) env->NewGlobalRef(env->FindClass("org/plovdev/audioengine/tracks/format/TrackFormat"));
    ctorTrackFormat = env->GetMethodID(
            clsTrackFormat,
            "<init>",
            "(Ljava/lang/String;IIIZLjava/nio/ByteOrder;)V"
    );

    if (!clsTrackFormat || !ctorTrackFormat) {
        std::cout << "null <init>. return..." << std::endl;
        return set;
    }

    // Получаем потоки устройства (этот блок остается прежним)
    UInt32 size = 0;
    AudioObjectPropertyAddress addrStreams{
        kAudioDevicePropertyStreams, kAudioObjectPropertyScopeGlobal, kAudioObjectPropertyElementMaster
    };
    if (AudioObjectGetPropertyDataSize(devId, &addrStreams, 0, nullptr, &size) != noErr) { return set; }

    UInt32 streamCount = size / sizeof(AudioStreamID);
    std::vector<AudioStreamID> streams(streamCount);
    if (AudioObjectGetPropertyData(devId, &addrStreams, 0, nullptr, &size, streams.data()) != noErr) { return set; }

    for (UInt32 i = 0; i < streamCount; i++) {
        AudioStreamID streamID = streams[i];

        // !!! КЛЮЧЕВОЕ ИЗМЕНЕНИЕ ЗДЕСЬ !!!
        // Запрашиваем ВСЕ доступные физические форматы
        UInt32 availableFormatsSize = 0;
        AudioObjectPropertyAddress addrFormats{
            kAudioStreamPropertyPhysicalFormats,
            kAudioObjectPropertyScopeGlobal,
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

        // Итерируемся по КАЖДОМУ доступному формату
        for (const auto& asbd : availableFormats) {
             // Можно добавить фильтр по каналам обратно, если нужно:
             if ((int)asbd.mChannelsPerFrame != channels) continue;

            bool isSigned = (asbd.mFormatFlags & kAudioFormatFlagIsSignedInteger) != 0;
            bool isFloat  = (asbd.mFormatFlags & kAudioFormatFlagIsFloat) != 0;
            jboolean signedFlag = isSigned || isFloat;

            // Создаём TrackFormat и добавляем в HashSet
            jobject tf = env->NewObject(
                clsTrackFormat, ctorTrackFormat,
                env->NewStringUTF("wav"),
                (jint)asbd.mChannelsPerFrame,
                (jint)asbd.mBitsPerChannel,
                (jint)asbd.mSampleRate,
                signedFlag,
                byteOrderLE
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
        if (!initJNI(env, "org/plovdev/audioengine/devices/NativeInputAudioDevice")) {
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
            // Check for micro
            AudioObjectPropertyAddress propAddr = { kAudioDevicePropertyStreamConfiguration, kAudioObjectPropertyScopeInput, kAudioObjectPropertyElementMaster };
            UInt32 deviceSize = 0;
            AudioObjectGetPropertyDataSize(devId, &propAddr, 0, nullptr, &deviceSize);
            AudioBufferList* bufferList = (AudioBufferList*)malloc(deviceSize);
            AudioObjectGetPropertyData(devId, &propAddr, 0, nullptr, &deviceSize, bufferList);

            int inputChannels = 0;
            for (UInt32 i = 0; i < bufferList->mNumberBuffers; i++) {
                inputChannels += bufferList->mBuffers[i].mNumberChannels;
            }
            free(bufferList);

            if (inputChannels == 0) continue; // не входное устройство




            CFStringRef nameRef = nullptr;
            UInt32 propSize = sizeof(nameRef);

            AudioObjectPropertyAddress nameAddr{kAudioObjectPropertyName,kAudioObjectPropertyScopeInput,kAudioObjectPropertyElementMaster};
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

            jobject info = createAudioDeviceInfo(env, devId, name, inputChannels);
            if (!info) continue;

            jobject device = env->NewObject(clsInputDevice, ctorInputDevice, info);
            if (!device) continue;

            env->CallBooleanMethod(list, arrayListAdd, device);
        }

        return list;
    }



    JNIEXPORT jobject JNICALL Java_org_plovdev_audioengine_devices_AudioDeviceManager_getOutputDevices(JNIEnv* env, jobject jobj) {
        if (!initJNI(env, "org/plovdev/audioengine/devices/NativeOutputAudioDevice")) {
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
            // Проверяем, что это выходное устройство
            AudioObjectPropertyAddress streamAddr{ kAudioDevicePropertyStreamConfiguration, kAudioObjectPropertyScopeOutput, kAudioObjectPropertyElementMaster };
            UInt32 streamSize = 0;
            if (AudioObjectGetPropertyDataSize(devId, &streamAddr, 0, nullptr, &streamSize) != noErr || streamSize == 0) {
                continue; // не выходное устройство
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

            if (outputChannels == 0) continue; // не выходное устройство

            CFStringRef nameRef = nullptr;
            UInt32 propSize = sizeof(nameRef);

            AudioObjectPropertyAddress nameAddr{kAudioObjectPropertyName,kAudioObjectPropertyScopeInput,kAudioObjectPropertyElementMaster};
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

            jobject info = createAudioDeviceInfo(env, devId, name, 2);
            if (!info) continue;

            jobject device = env->NewObject(clsInputDevice, ctorInputDevice, info);
            if (!device) continue;

            env->CallBooleanMethod(list, arrayListAdd, device);
        }

        return list;
    }
}