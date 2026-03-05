#ifndef TrackFormat_H
#define TrackFormat_H

#include <string>
#include <stdexcept>
#include "AudioCodec.h"

namespace audiosnd {
    class TrackFormat {
    private:
        int m_channels;
        int m_bitDepth;
        int m_sampleRate;
        bool m_signed;
        AudioCodec m_audioCodec;

    public:
        TrackFormat(int channels, int bitDepth, int sampleRate, bool signed_, AudioCodec codec)
            : m_channels(channels)
            , m_bitDepth(bitDepth)
            , m_sampleRate(sampleRate)
            , m_signed(signed_)
            , m_audioCodec(codec) {
        }

        // Getters
        int channels() const { return m_channels; }
        int bitDepth() const { return m_bitDepth; }
        int sampleRate() const { return m_sampleRate; }
        bool signed_() const { return m_signed; }
        AudioCodec audioCodec() const { return m_audioCodec; }

        // Вычисление битрейта
        int bitRate() const {
            return m_sampleRate * m_bitDepth * m_channels;
        }

        // Байт на фрейм
        int bytesPerFrame() const {
            return (m_bitDepth / 8) * m_channels;
        }

        // Проверка, является ли PCM
        bool isPCM() const {
            return isUncompressedPCM(m_audioCodec);
        }

        // toString
        std::string toString() const {
            return std::to_string(m_sampleRate) + "Hz, " +
                   std::to_string(m_channels) + "ch, " +
                   (m_signed ? "signed" : "unsigned") + ", " +
                   audioCodecToString(m_audioCodec);
        }

        // equals
        bool operator==(const TrackFormat& other) const {
            return m_channels == other.m_channels &&
                   m_bitDepth == other.m_bitDepth &&
                   m_sampleRate == other.m_sampleRate &&
                   m_signed == other.m_signed &&
                   m_audioCodec == other.m_audioCodec;
        }
    };
}

#endif