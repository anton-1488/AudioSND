#ifndef AudioDeviceInfo_H
#define AudioDeviceInfo_H

#include <string>
#include <vector>
#include "AudioCodec.h"
#include "TrackFormat.h"

namespace audiosnd {
    enum class AudioDeviceType {
        INPUT,      // Input device (microphone, line-in)
        OUTPUT,     // Output device (speakers, headphones)
        DUPLEX      // Full-duplex device (input and output simultaneously)
    };

    inline const char* audioDeviceTypeToString(AudioDeviceType type) {
        switch(type) {
            case AudioDeviceType::INPUT:  return "INPUT";
            case AudioDeviceType::OUTPUT: return "OUTPUT";
            case AudioDeviceType::DUPLEX: return "DUPLEX";
            default: return "UNKNOWN";
        }
    }

    class AudioDeviceInfo {
    private:
        std::string m_id;                    // Уникальный идентификатор
        std::string m_name;                   // Имя устройства
        std::string m_vendor;                  // Производитель
        AudioDeviceType m_type;                // Тип устройства
        std::vector<TrackFormat> m_supportedFormats;  // Поддерживаемые форматы

    public:
        // Конструктор
        AudioDeviceInfo(
            const std::string& id,
            const std::string& name,
            const std::string& vendor,
            AudioDeviceType type,
            const std::vector<TrackFormat>& formats
        ) : m_id(id)
            , m_name(name)
            , m_vendor(vendor)
            , m_type(type)
            , m_supportedFormats(formats) {}

        // Getters (как в Java record)
        const std::string& id() const { return m_id; }
        const std::string& name() const { return m_name; }
        const std::string& vendor() const { return m_vendor; }
        AudioDeviceType type() const { return m_type; }
        const std::vector<TrackFormat>& supportedFormats() const { return m_supportedFormats; }

        // Удобные методы проверки типа
        bool isInput() const {
            return m_type == AudioDeviceType::INPUT ||
                   m_type == AudioDeviceType::DUPLEX;
        }

        bool isOutput() const {
            return m_type == AudioDeviceType::OUTPUT ||
                   m_type == AudioDeviceType::DUPLEX;
        }

        bool isDuplex() const {
            return m_type == AudioDeviceType::DUPLEX;
        }

        // Добавить формат (удобно при построении)
        void addFormat(const TrackFormat& format) {
            m_supportedFormats.push_back(format);
        }

        // toString (как в Java)
        std::string toString() const {
            std::string result = m_name + " (" + m_id + ") - " +
                                audioDeviceTypeToString(m_type);

            if (!m_vendor.empty()) {
                result += " by " + m_vendor;
            }

            result += ", supports " + std::to_string(m_supportedFormats.size()) + " formats";

            return result;
        }

        // equals (по id, как в Java)
        bool operator==(const AudioDeviceInfo& other) const {
            return m_id == other.m_id;
        }

        bool operator!=(const AudioDeviceInfo& other) const {
            return !(*this == other);
        }

        // Для сортировки (по имени)
        bool operator<(const AudioDeviceInfo& other) const {
            return m_name < other.m_name;
        }
    };

    // Для использования в коллекциях
    using AudioDeviceInfoList = std::vector<AudioDeviceInfo>;
}

namespace std {
    template<>
    struct hash<audiosnd::AudioDeviceInfo> {
        size_t operator()(const audiosnd::AudioDeviceInfo& info) const {
            return hash<string>{}(info.id());
        }
    };
}

#endif