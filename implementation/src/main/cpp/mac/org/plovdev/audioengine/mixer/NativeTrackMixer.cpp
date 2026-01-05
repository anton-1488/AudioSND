#include <jni.h>
#include <vector>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <memory>
#include <stdexcept>

struct TrackData {
    std::vector<float> samples; // нормализуем в float [-1.0, 1.0]
    int channels;
    int sampleRate;
};

// Конвертация PCM в float
float pcmToFloat(const uint8_t* data, int bitsPerSample, int channel, int channels) {
    if (bitsPerSample == 8) {
        int8_t sample = ((int8_t*)data)[channel];
        return sample / 128.0f;
    } else if (bitsPerSample == 16) {
        int16_t* ptr = (int16_t*)data;
        return ptr[channel] / 32768.0f;
    } else if (bitsPerSample == 24) {
        const uint8_t* ptr = data + channel * 3;
        // Little-endian для 24-бит
        int32_t sample = (ptr[0] << 8) | (ptr[1] << 16) | (ptr[2] << 24);
        sample >>= 8; // Приводим к signed 24-bit
        return sample / 8388608.0f;
    } else if (bitsPerSample == 32) {
        int32_t* ptr = (int32_t*)data;
        return ptr[channel] / 2147483648.0f;
    } else {
        return 0.0f;
    }
}

// Конвертация float в PCM
void floatToPcm(float sample, uint8_t* out, int bitsPerSample) {
    sample = std::clamp(sample, -1.0f, 1.0f);
    if (bitsPerSample == 8) {
        out[0] = (uint8_t)((int8_t)(sample * 127));
    } else if (bitsPerSample == 16) {
        int16_t* ptr = (int16_t*)out;
        ptr[0] = (int16_t)(sample * 32767);
    } else if (bitsPerSample == 24) {
        int32_t val = (int32_t)(sample * 8388607);
        // Little-endian
        out[0] = val & 0xFF;
        out[1] = (val >> 8) & 0xFF;
        out[2] = (val >> 16) & 0xFF;
    } else if (bitsPerSample == 32) {
        int32_t* ptr = (int32_t*)out;
        ptr[0] = (int32_t)(sample * 2147483647);
    }
}

// Безопасный ресемплинг
std::vector<float> resample(const std::vector<float>& input, int inRate, int outRate, int channels) {
    if (inRate == outRate || input.empty()) return input;

    size_t inFrames = input.size() / channels;
    if (inFrames == 0) return {};

    size_t outFrames = inFrames * outRate / inRate;
    size_t outSize = outFrames * channels;

    std::vector<float> out(outSize, 0.0f);

    for (size_t i = 0; i < outFrames; ++i) {
        for (int ch = 0; ch < channels; ++ch) {
            float pos = (float)i * inRate / outRate;
            int idx = (int)pos;

            if (idx >= (int)inFrames - 1) {
                // За пределами - используем последний доступный сэмпл
                int lastIdx = std::min((int)inFrames - 1, idx) * channels + ch;
                if (lastIdx < (int)input.size()) {
                    out[i * channels + ch] = input[lastIdx];
                }
                continue;
            }

            float frac = pos - idx;
            int idx0 = idx * channels + ch;
            int idx1 = (idx + 1) * channels + ch;

            if (idx0 < (int)input.size() && idx1 < (int)input.size()) {
                out[i * channels + ch] = input[idx0] + frac * (input[idx1] - input[idx0]);
            }
        }
    }
    return out;
}

// Безопасное микширование
std::vector<float> mixTracks(const std::vector<TrackData>& tracks, int outChannels, int outSampleRate) {
    if (tracks.empty()) return {};

    // Находим максимальную длину в кадрах (frames)
    size_t maxFrames = 0;
    for (auto& t : tracks) {
        if (t.samples.empty() || t.channels == 0) continue;

        size_t frames = t.samples.size() / t.channels;
        size_t resampledFrames = frames * outSampleRate / t.sampleRate;
        maxFrames = std::max(maxFrames, resampledFrames);
    }

    if (maxFrames == 0) return {};

    size_t maxLen = maxFrames * outChannels;
    std::vector<float> mix(maxLen, 0.0f);

    for (auto& t : tracks) {
        if (t.samples.empty() || t.channels == 0) continue;

        std::vector<float> resampled = resample(t.samples, t.sampleRate, outSampleRate, t.channels);

        // Адаптация количества каналов
        if (t.channels != outChannels) {
            std::vector<float> adapted(maxLen, 0.0f);
            size_t frames = std::min(resampled.size() / t.channels, maxFrames);

            for (size_t i = 0; i < frames; ++i) {
                for (int ch = 0; ch < outChannels; ++ch) {
                    int srcCh = (outChannels == 1) ? 0 : std::min(ch, t.channels-1);
                    int srcIdx = i * t.channels + srcCh;
                    if (srcIdx < (int)resampled.size()) {
                        adapted[i * outChannels + ch] = resampled[srcIdx];
                    }
                }
            }
            resampled = std::move(adapted);
        }

        // Сложение с проверкой границ
        size_t mixSize = std::min(mix.size(), resampled.size());
        for (size_t i = 0; i < mixSize; ++i) {
            mix[i] += resampled[i];
        }
    }

    // Нормализация
    float maxAmp = 0.01f; // минимальное значение для избежания деления на 0
    for (auto v : mix) {
        maxAmp = std::max(maxAmp, std::abs(v));
    }
    if (maxAmp > 1.0f) {
        for (auto &v : mix) v /= maxAmp;
    }

    return mix;
}

// JNI wrapper
extern "C"
JNIEXPORT jobject JNICALL Java_org_plovdev_audioengine_mixer_NativeTrackMixer__1doMixing(
    JNIEnv* env, jobject obj, jobject trackList, jobject formatObj) {

    // Проверка входных параметров
    if (!env || !trackList || !formatObj) {
        return nullptr;
    }

    try {
        jclass trackCls = env->FindClass("org/plovdev/audioengine/tracks/Track");
        if (!trackCls) {
            env->ThrowNew(env->FindClass("java/lang/ClassNotFoundException"),
                         "Track class not found");
            return nullptr;
        }

        jmethodID getTrackData = env->GetMethodID(trackCls, "getTrackData", "()Ljava/nio/ByteBuffer;");
        jmethodID getFormat = env->GetMethodID(trackCls, "getFormat", "()Lorg/plovdev/audioengine/tracks/format/TrackFormat;");

        jclass formatCls = env->FindClass("org/plovdev/audioengine/tracks/format/TrackFormat");
        if (!formatCls) {
            env->ThrowNew(env->FindClass("java/lang/ClassNotFoundException"),
                         "TrackFormat class not found");
            return nullptr;
        }

        jmethodID getChannels = env->GetMethodID(formatCls, "channels", "()I");
        jmethodID getSampleRate = env->GetMethodID(formatCls, "sampleRate", "()I");
        jmethodID getBitsPerSample = env->GetMethodID(formatCls, "bitsPerSample", "()I");

        jclass listCls = env->GetObjectClass(trackList);
        jmethodID sizeID = env->GetMethodID(listCls, "size", "()I");
        jmethodID getID = env->GetMethodID(listCls, "get", "(I)Ljava/lang/Object;");

        int listSize = env->CallIntMethod(trackList, sizeID);
        if (listSize <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
                         "Empty track list");
            return nullptr;
        }

        std::vector<TrackData> tracks;
        tracks.reserve(listSize);

        for (int i = 0; i < listSize; ++i) {
            jobject track = env->CallObjectMethod(trackList, getID, i);
            if (!track) continue;

            jobject buffer = env->CallObjectMethod(track, getTrackData);
            if (!buffer) continue;

            uint8_t* bufPtr = (uint8_t*)env->GetDirectBufferAddress(buffer);
            jlong bufSize = env->GetDirectBufferCapacity(buffer);

            if (!bufPtr || bufSize <= 0) continue;

            jobject fmt = env->CallObjectMethod(track, getFormat);
            if (!fmt) continue;

            int ch = env->CallIntMethod(fmt, getChannels);
            int rate = env->CallIntMethod(fmt, getSampleRate);
            int bps = env->CallIntMethod(fmt, getBitsPerSample);

            if (ch <= 0 || rate <= 0 || bps <= 0) continue;

            std::vector<float> samples;
            int bytesPerFrame = (bps * ch + 7) / 8; // байт на кадр

            for (jlong j = 0; j < bufSize; j += bytesPerFrame) {
                for (int c = 0; c < ch; ++c) {
                    samples.push_back(pcmToFloat(bufPtr + j, bps, c, ch));
                }
            }

            if (!samples.empty()) {
                tracks.push_back({samples, ch, rate});
            }
        }

        if (tracks.empty()) {
            env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
                         "No valid tracks to mix");
            return nullptr;
        }

        // Получаем выходной формат
        jclass outputFormatCls = env->GetObjectClass(formatObj);
        jmethodID outputGetChannels = env->GetMethodID(outputFormatCls, "channels", "()I");
        jmethodID outputGetSampleRate = env->GetMethodID(outputFormatCls, "sampleRate", "()I");
        jmethodID outputGetBitsPerSample = env->GetMethodID(outputFormatCls, "bitsPerSample", "()I");

        int outChannels = env->CallIntMethod(formatObj, outputGetChannels);
        int outSampleRate = env->CallIntMethod(formatObj, outputGetSampleRate);
        int outBits = env->CallIntMethod(formatObj, outputGetBitsPerSample);

        if (outChannels <= 0 || outSampleRate <= 0 || outBits <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
                         "Invalid output format");
            return nullptr;
        }

        // Микшируем
        std::vector<float> mixed = mixTracks(tracks, outChannels, outSampleRate);

        if (mixed.empty()) {
            env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
                         "Mixing produced no audio");
            return nullptr;
        }

        // Создаем DirectByteBuffer
        int outBytesPerSample = (outBits + 7) / 8;
        size_t byteSize = mixed.size() * outBytesPerSample;

        uint8_t* buffer = new uint8_t[byteSize];
        std::fill(buffer, buffer + byteSize, 0);

        for (size_t i = 0; i < mixed.size(); ++i) {
            floatToPcm(mixed[i], buffer + i * outBytesPerSample, outBits);
        }

        jobject resultBuffer = env->NewDirectByteBuffer(buffer, byteSize);

        // Вычисляем длительность
        double seconds = (double)mixed.size() / (outSampleRate * outChannels);
        jlong millis = (jlong)(seconds * 1000);

        jclass durationCls = env->FindClass("java/time/Duration");
        jmethodID ofMillis = env->GetStaticMethodID(durationCls, "ofMillis", "(J)Ljava/time/Duration;");
        jobject durationObj = env->CallStaticObjectMethod(durationCls, ofMillis, millis);

        jclass metadataCls = env->FindClass("org/plovdev/audioengine/tracks/meta/TrackMetadata");
        jmethodID metadataCtor = env->GetMethodID(metadataCls, "<init>", "()V");
        jobject metadataObj = env->NewObject(metadataCls, metadataCtor);

        // Создаем новый Track объект
        jmethodID trackCtor = env->GetMethodID(trackCls, "<init>", "(Ljava/nio/ByteBuffer;Ljava/time/Duration;Lorg/plovdev/audioengine/tracks/format/TrackFormat;Lorg/plovdev/audioengine/tracks/meta/TrackMetadata;)V");
        jobject newTrack = env->NewObject(trackCls, trackCtor, resultBuffer, durationObj, formatObj, metadataObj);

        // Освобождаем память (в продакшене нужно использовать finalizer или Cleaner)
        // delete[] buffer; // Не удаляем - буфер принадлежит Java DirectByteBuffer

        return newTrack;

    } catch (const std::exception& e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    } catch (...) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"),
                     "Unknown native error during mixing");
        return nullptr;
    }
}