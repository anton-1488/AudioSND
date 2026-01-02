#include <iostream>
#include <jni.h>
#include <vector>
#include <cstring>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <memory>

#include "org_plovdev_audioengine_mixer_NativeTrackMixer.h"

using namespace std;

// Структура для хранения аудиоданных в едином формате (float)
struct AudioData {
    vector<float> samples;  // интерливированные семплы [L,R,L,R,...]
    int channels;
    int sampleRate;
    size_t frameCount;      // количество фреймов (семплов на канал)

    AudioData() : channels(0), sampleRate(0), frameCount(0) {}
};

void thowException(JNIEnv* env, const char* message) {
    jclass exClass = env->FindClass("org/plovdev/audioengine/exceptions/MixingException");
    if (exClass != nullptr) {
        // Создаем строку сообщения
        jstring jMessage = env->NewStringUTF(message);

        // Создаем объект исключения
        jmethodID constructor = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;I)V");
        jobject exception = env->NewObject(exClass, constructor, jMessage, errorCode);

        env->Throw((jthrowable)exception);

        env->DeleteLocalRef(jMessage);
        env->DeleteLocalRef(exception);
    }
}

// Конвертация байтов в float в зависимости от формата
vector<float> convertToFloat(const void* data, size_t byteCount,
                             int bitsPerSample, int channels, bool isSigned) {
    size_t sampleCount = byteCount / (bitsPerSample / 8);
    vector<float> result(sampleCount);

    if (bitsPerSample == 16 && isSigned) {
        const int16_t* src = static_cast<const int16_t*>(data);
        for (size_t i = 0; i < sampleCount; i++) {
            result[i] = src[i] / 32768.0f;  // [-1.0, 1.0]
        }
    }
    else if (bitsPerSample == 32) {
        const float* src = static_cast<const float*>(data);
        for (size_t i = 0; i < sampleCount; i++) {
            result[i] = src[i];  // уже float
        }
    }
    else if (bitsPerSample == 8 && !isSigned) {
        const uint8_t* src = static_cast<const uint8_t*>(data);
        for (size_t i = 0; i < sampleCount; i++) {
            result[i] = (src[i] - 128) / 128.0f;  // uint8 -> [-1.0, 1.0]
        }
    }

    return result;
}

// Конвертация float в нужный выходной формат
vector<uint8_t> convertFromFloat(const vector<float>& samples,
                                 int bitsPerSample, bool isSigned) {
    size_t sampleCount = samples.size();
    vector<uint8_t> result(sampleCount * (bitsPerSample / 8));

    if (bitsPerSample == 16 && isSigned) {
        int16_t* dst = reinterpret_cast<int16_t*>(result.data());
        for (size_t i = 0; i < sampleCount; i++) {
            // Клиппинг
            float clipped = fmaxf(-1.0f, fminf(1.0f, samples[i]));
            dst[i] = static_cast<int16_t>(clipped * 32767.0f);
        }
    }
    else if (bitsPerSample == 32) {
        float* dst = reinterpret_cast<float*>(result.data());
        for (size_t i = 0; i < sampleCount; i++) {
            dst[i] = samples[i];  // просто копируем float
        }
    }

    return result;
}

// Ресамплинг (простая линейная интерполяция)
vector<float> resample(const vector<float>& input, int inputRate, int outputRate, int channels) {
    if (inputRate == outputRate) return input;

    float ratio = static_cast<float>(inputRate) / outputRate;
    size_t outputFrames = static_cast<size_t>(input.size() / channels / ratio);
    size_t outputSamples = outputFrames * channels;

    vector<float> result(outputSamples);

    for (size_t i = 0; i < outputSamples; i++) {
        float pos = i * ratio;
        size_t idx1 = static_cast<size_t>(pos);
        size_t idx2 = min(idx1 + 1, input.size() - 1);
        float frac = pos - idx1;

        result[i] = input[idx1] * (1.0f - frac) + input[idx2] * frac;
    }

    return result;
}

// Конвертация каналов (моно <-> стерео)
vector<float> convertChannels(const vector<float>& input, int inputChannels, int outputChannels) {
    if (inputChannels == outputChannels) return input;

    size_t inputFrames = input.size() / inputChannels;
    vector<float> result(inputFrames * outputChannels);

    if (inputChannels == 1 && outputChannels == 2) {
        // Моно -> стерео (дублируем)
        for (size_t i = 0; i < inputFrames; i++) {
            result[i * 2] = input[i];      // левый
            result[i * 2 + 1] = input[i];  // правый
        }
    }
    else if (inputChannels == 2 && outputChannels == 1) {
        // Стерео -> моно (усредняем)
        for (size_t i = 0; i < inputFrames; i++) {
            result[i] = (input[i * 2] + input[i * 2 + 1]) * 0.5f;
        }
    }

    return result;
}

// Основная функция микширования
AudioData mixTracks(const vector<AudioData>& tracks, int outputChannels, int outputSampleRate) {
    AudioData result;
    result.channels = outputChannels;
    result.sampleRate = outputSampleRate;

    // Находим максимальную длину
    size_t maxFrames = 0;
    for (const auto& track : tracks) {
        size_t trackFrames = track.frameCount;
        maxFrames = max(maxFrames, trackFrames);
    }

    if (maxFrames == 0) {
        return result;  // пустой результат
    }

    size_t totalSamples = maxFrames * outputChannels;
    result.samples.resize(totalSamples, 0.0f);
    result.frameCount = maxFrames;

    // Микшируем все треки
    for (const auto& track : tracks) {
        vector<float> processed = track.samples;

        // Ресамплинг если нужно
        if (track.sampleRate != outputSampleRate) {
            processed = resample(processed, track.sampleRate, outputSampleRate, track.channels);
        }

        // Конвертация каналов если нужно
        if (track.channels != outputChannels) {
            size_t framesAfterResample = processed.size() / track.channels;
            processed = convertChannels(processed, track.channels, outputChannels);
        }

        // Добавляем к результату (простое сложение)
        for (size_t i = 0; i < min(processed.size(), totalSamples); i++) {
            result.samples[i] += processed[i];
        }
    }

    // Нормализация (предотвращение клиппинга)
    float maxSample = 0.0f;
    for (float sample : result.samples) {
        maxSample = max(maxSample, fabsf(sample));
    }

    if (maxSample > 1.0f) {
        float gain = 0.99f / maxSample;  // оставляем небольшой запас
        for (float& sample : result.samples) {
            sample *= gain;
        }
    }

    return result;
}

// Создание Java Track объекта
jobject createJavaTrack(JNIEnv* env, const vector<uint8_t>& data, jobject outputFormat, long durationMillis) {

    try {
        // 1. Создаем ByteBuffer
        jobject byteBuffer = env->NewDirectByteBuffer(
            const_cast<uint8_t*>(data.data()),
            data.size()
        );

        // 2. Создаем Duration
        jclass durationClass = env->FindClass("java/time/Duration");
        jmethodID ofMillisMethod = env->GetStaticMethodID(
            durationClass, "ofMillis", "(J)Ljava/time/Duration;");
        jobject duration = env->CallStaticObjectMethod(
            durationClass, ofMillisMethod, (jlong)durationMillis);

        // 3. Создаем TrackMetadata
        jclass metadataClass = env->FindClass("org/plovdev/audioengine/tracks/meta/TrackMetadata");
        jmethodID createDefaultMethod = env->GetMethodID(metadataClass, "<init>", "()V");

        jobject metadata = env->NewObject(metadataClass, createDefaultMethod);

        // 4. Создаем Track
        jclass trackClass = env->FindClass("org/plovdev/audioengine/tracks/Track");
        jmethodID constructor = env->GetMethodID(trackClass, "<init>",
            "(Ljava/nio/ByteBuffer;Ljava/time/Duration;Lorg/plovdev/audioengine/tracks/format/TrackFormat;Lorg/plovdev/audioengine/tracks/meta/TrackMetadata;)V");

        jobject track = env->NewObject(trackClass, constructor,
            byteBuffer, duration, outputFormat, metadata);

        // 5. Очищаем локальные ссылки
        env->DeleteLocalRef(byteBuffer);
        env->DeleteLocalRef(duration);
        env->DeleteLocalRef(metadata);

        return track;

    } catch (...) {
        return nullptr;
    }
}

extern "C" {

    JNIEXPORT jobject JNICALL Java_org_plovdev_audioengine_mixer_NativeTrackMixer__1doMixing(
        JNIEnv* env, jobject jobj, jobject mixingTracks, jobject outputFormat) {

        try {
            // 1. Получаем параметры выходного формата
            jclass formatClass = env->GetObjectClass(outputFormat);
            jmethodID channelsMethod = env->GetMethodID(formatClass, "channels", "()I");
            jmethodID sampleRateMethod = env->GetMethodID(formatClass, "sampleRate", "()I");
            jmethodID bitsMethod = env->GetMethodID(formatClass, "bitsPerSample", "()I");
            jmethodID signedMethod = env->GetMethodID(formatClass, "signed", "()Z");

            int outputChannels = env->CallIntMethod(outputFormat, channelsMethod);
            int outputSampleRate = env->CallIntMethod(outputFormat, sampleRateMethod);
            int outputBitsPerSample = env->CallIntMethod(outputFormat, bitsMethod);
            bool outputSigned = env->CallBooleanMethod(outputFormat, signedMethod);

            // 2. Получаем список треков
            jclass listClass = env->FindClass("java/util/List");
            jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
            jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");

            jint trackCount = env->CallIntMethod(mixingTracks, sizeMethod);

            if (trackCount == 0) {
                thowException(env, "Track count can't be empty");
                return nullptr;
            }

            // 3. Собираем данные всех треков
            vector<AudioData> tracks;

            jclass trackClass = env->FindClass("org/plovdev/audioengine/tracks/Track");
            jmethodID getTrackDataMethod = env->GetMethodID(trackClass, "getTrackData",
                "()Ljava/nio/ByteBuffer;");
            jmethodID getFormatMethod = env->GetMethodID(trackClass, "getFormat",
                "()Lorg/plovdev/audioengine/tracks/format/TrackFormat;");
            jmethodID getDurationMethod = env->GetMethodID(trackClass, "getDuration",
                "()Ljava/time/Duration;");
            jclass durationClass = env->FindClass("java/time/Duration");
            jmethodID toMillisMethod = env->GetMethodID(durationClass, "toMillis", "()J");

            for (jint i = 0; i < trackCount; i++) {
                jobject track = env->CallObjectMethod(mixingTracks, getMethod, i);

                // Получаем ByteBuffer с данными
                jobject byteBuffer = env->CallObjectMethod(track, getTrackDataMethod);
                void* bufferPtr = env->GetDirectBufferAddress(byteBuffer);
                jlong bufferSize = env->GetDirectBufferCapacity(byteBuffer);

                if (!bufferPtr || bufferSize == 0) {
                    continue;
                }

                // Получаем формат трека
                jobject trackFormat = env->CallObjectMethod(track, getFormatMethod);
                int channels = env->CallIntMethod(trackFormat, channelsMethod);
                int sampleRate = env->CallIntMethod(trackFormat, sampleRateMethod);
                int bitsPerSample = env->CallIntMethod(trackFormat, bitsMethod);
                bool isSigned = env->CallBooleanMethod(trackFormat, signedMethod);

                // Получаем длительность
                jobject duration = env->CallObjectMethod(track, getDurationMethod);
                jlong durationMillis = env->CallLongMethod(duration, toMillisMethod);

                // Конвертируем в float
                vector<float> floatSamples = convertToFloat(
                    bufferPtr, bufferSize, bitsPerSample, channels, isSigned);

                AudioData audioData;
                audioData.samples = floatSamples;
                audioData.channels = channels;
                audioData.sampleRate = sampleRate;
                audioData.frameCount = floatSamples.size() / channels;

                tracks.push_back(audioData);

                // Очищаем локальные ссылки
                env->DeleteLocalRef(track);
                env->DeleteLocalRef(trackFormat);
                env->DeleteLocalRef(duration);
                env->DeleteLocalRef(byteBuffer);
            }

            if (tracks.empty()) {
                thowException(env, "Tracks is empty");
                return nullptr;
            }

            AudioData mixed = mixTracks(tracks, outputChannels, outputSampleRate);

            if (mixed.samples.empty()) {
                thowException(env, "Track mixed samples is empty");
                return nullptr;
            }

            vector<uint8_t> outputData = convertFromFloat(
                mixed.samples, outputBitsPerSample, outputSigned);

            // 6. Рассчитываем длительность результата
            long durationMillis = (mixed.frameCount * 1000L) / outputSampleRate;

            // 7. Создаем Java Track объект
            jobject result = createJavaTrack(env, outputData, outputFormat, durationMillis);
            return result;

        } catch (...) {
            thowException(env, "Unknown mixing error");
            return nullptr;
        }
    }
}