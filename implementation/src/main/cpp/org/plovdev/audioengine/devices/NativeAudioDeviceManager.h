// org/plovdev/audioengine/devices/NativeAudioDeviceManager.h
#ifndef NativeAudioDeviceManager_H
#define NativeAudioDeviceManager_H

#include <string>
#include <vector>
#include <memory>
#include "../TrackFormat.h"
#include "../AudioDeviceInfo.h"

namespace audiosnd {
class NativeAudioDeviceManager {
public:
    virtual ~NativeAudioDeviceManager() = default;

    // Получить все устройства (возвращает наши C++ DTO)
    virtual std::vector<AudioDeviceInfo> getAllDevices() = 0;

    // Получить только входные устройства
    virtual std::vector<AudioDeviceInfo> getInputDevices() = 0;

    // Получить только выходные устройства
    virtual std::vector<AudioDeviceInfo> getOutputDevices() = 0;

    // Получить устройство по умолчанию
    virtual AudioDeviceInfo getDefaultInputDevice() = 0;
    virtual AudioDeviceInfo getDefaultOutputDevice() = 0;

    // Получить устройство по ID
    virtual AudioDeviceInfo getDeviceById(const std::string& id) = 0;

    // Фабрика для создания правильной реализации под текущую ОС
    static std::unique_ptr<NativeAudioDeviceManager> create();
};

} // namespace audiosnd

#endif