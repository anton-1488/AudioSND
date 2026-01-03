#include <iostream>
#include <jni.h>
#include <vector>
#include <cstring>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <memory>
#include <mutex>
#include <atomic>
#include <immintrin.h>

#include "org_plovdev_audioengine_mixer_NativeTrackMixer.h"

using namespace std;

// Включить SIMD оптимизации если доступно
#ifdef __AVX2__
#define USE_SIMD 1
#else
#define USE_SIMD 0
#endif

// Структура для хранения аудиоданных
struct AudioData {
    vector<float> samples;      // интерливированные семплы [L,R,L,R,...]
    int channels;
    int sampleRate;
    size_t frameCount;          // количество фреймов (семплов на канал)

    AudioData() : channels(0), sampleRate(0), frameCount(0) {}

    // Оптимизированный конструктор перемещения
    AudioData(AudioData&& other) noexcept
        : samples(move(other.samples))
        , channels(other.channels)
        , sampleRate(other.sampleRate)
        , frameCount(other.frameCount) {
        other.channels = 0;
        other.sampleRate = 0;
        other.frameCount = 0;
    }

    AudioData& operator=(AudioData&& other) noexcept {
        if (this != &other) {
            samples = move(other.samples);
            channels = other.channels;
            sampleRate = other.sampleRate;
            frameCount = other.frameCount;
            other.channels = 0;
            other.sampleRate = 0;
            other.frameCount = 0;
        }
        return *this;
    }

    // Запрещаем копирование для избежания накладных расходов
    AudioData(const AudioData&) = delete;
    AudioData& operator=(const AudioData&) = delete;
};

// Кэш для JNI классов и методов
struct JNICache {
    jclass trackClass = nullptr;
    jclass formatClass = nullptr;
    jclass durationClass = nullptr;
    jclass listClass = nullptr;
    jclass metadataClass = nullptr;
    jclass exceptionClass = nullptr;

    jmethodID formatChannels = nullptr;
    jmethodID formatSampleRate = nullptr;
    jmethodID formatBits = nullptr;
    jmethodID formatSigned = nullptr;

    jmethodID listSize = nullptr;
    jmethodID listGet = nullptr;

    jmethodID trackGetData = nullptr;
    jmethodID trackGetFormat = nullptr;
    jmethodID trackGetDuration = nullptr;
    jmethodID trackConstructor = nullptr;

    jmethodID durationToMillis = nullptr;
    jmethodID durationOfMillis = nullptr;

    jmethodID exceptionConstructor = nullptr;

    atomic<bool> initialized{false};
    mutex initMutex;

    ~JNICache() {
        // JVM автоматически очистит глобальные ссылки при выгрузке нативной библиотеки
    }
};

static JNICache gCache;

// Инициализация кэша
void initCache(JNIEnv* env) {
    if (gCache.initialized.load()) return;

    lock_guard<mutex> lock(gCache.initMutex);
    if (gCache.initialized.load()) return;

    // Кэшируем классы как глобальные ссылки
    jclass localTrackClass = env->FindClass("org/plovdev/audioengine/tracks/Track");
    jclass localFormatClass = env->FindClass("org/plovdev/audioengine/tracks/format/TrackFormat");
    jclass localDurationClass = env->FindClass("java/time/Duration");
    jclass localListClass = env->FindClass("java/util/List");
    jclass localMetadataClass = env->FindClass("org/plovdev/audioengine/tracks/meta/TrackMetadata");
    jclass localExceptionClass = env->FindClass("org/plovdev/audioengine/exceptions/MixingException");

    if (!localTrackClass || !localFormatClass || !localDurationClass ||
        !localListClass || !localMetadataClass || !localExceptionClass) {
        // Не удалось найти классы
        return;
    }

    gCache.trackClass = static_cast<jclass>(env->NewGlobalRef(localTrackClass));
    gCache.formatClass = static_cast<jclass>(env->NewGlobalRef(localFormatClass));
    gCache.durationClass = static_cast<jclass>(env->NewGlobalRef(localDurationClass));
    gCache.listClass = static_cast<jclass>(env->NewGlobalRef(localListClass));
    gCache.metadataClass = static_cast<jclass>(env->NewGlobalRef(localMetadataClass));
    gCache.exceptionClass = static_cast<jclass>(env->NewGlobalRef(localExceptionClass));

    // Кэшируем методы
    gCache.formatChannels = env->GetMethodID(gCache.formatClass, "channels", "()I");
    gCache.formatSampleRate = env->GetMethodID(gCache.formatClass, "sampleRate", "()I");
    gCache.formatBits = env->GetMethodID(gCache.formatClass, "bitsPerSample", "()I");
    gCache.formatSigned = env->GetMethodID(gCache.formatClass, "signed", "()Z");

    gCache.listSize = env->GetMethodID(gCache.listClass, "size", "()I");
    gCache.listGet = env->GetMethodID(gCache.listClass, "get", "(I)Ljava/lang/Object;");

    gCache.trackGetData = env->GetMethodID(gCache.trackClass, "getTrackData", "()Ljava/nio/ByteBuffer;");
    gCache.trackGetFormat = env->GetMethodID(gCache.trackClass, "getFormat",
        "()Lorg/plovdev/audioengine/tracks/format/TrackFormat;");
    gCache.trackGetDuration = env->GetMethodID(gCache.trackClass, "getDuration",
        "()Ljava/time/Duration;");
    gCache.trackConstructor = env->GetMethodID(gCache.trackClass, "<init>",
        "(Ljava/nio/ByteBuffer;Ljava/time/Duration;Lorg/plovdev/audioengine/tracks/format/TrackFormat;Lorg/plovdev/audioengine/tracks/meta/TrackMetadata;)V");

    gCache.durationToMillis = env->GetMethodID(gCache.durationClass, "toMillis", "()J");
    gCache.durationOfMillis = env->GetStaticMethodID(gCache.durationClass, "ofMillis",
        "(J)Ljava/time/Duration;");

    gCache.exceptionConstructor = env->GetMethodID(gCache.exceptionClass, "<init>",
        "(Ljava/lang/String;)V");

    // Очищаем локальные ссылки
    env->DeleteLocalRef(localTrackClass);
    env->DeleteLocalRef(localFormatClass);
    env->DeleteLocalRef(localDurationClass);
    env->DeleteLocalRef(localListClass);
    env->DeleteLocalRef(localMetadataClass);
    env->DeleteLocalRef(localExceptionClass);

    gCache.initialized.store(true);
}

// RAII обертка для локального фрейма JNI
class JNILocalFrame {
    JNIEnv* env;
public:
    JNILocalFrame(JNIEnv* e, int capacity = 32) : env(e) {
        env->PushLocalFrame(capacity);
    }
    ~JNILocalFrame() {
        env->PopLocalFrame(nullptr);
    }
};

void throwException(JNIEnv* env, const char* message) {
    if (!gCache.initialized.load()) {
        initCache(env);
    }

    if (gCache.exceptionClass != nullptr) {
        jstring jMessage = env->NewStringUTF(message);
        jobject exception = env->NewObject(gCache.exceptionClass,
                                          gCache.exceptionConstructor, jMessage);
        env->Throw(static_cast<jthrowable>(exception));
        env->DeleteLocalRef(jMessage);
        env->DeleteLocalRef(exception);
    }
}

// Оптимизированная SIMD функция сложения
#if USE_SIMD
void addSamplesSIMD(float* dst, const float* src, size_t count) {
    size_t i = 0;

    // Обрабатываем по 8 семплов за раз (AVX)
    for (; i + 7 < count; i += 8) {
        __m256 dstVec = _mm256_loadu_ps(&dst[i]);
        __m256 srcVec = _mm256_loadu_ps(&src[i]);
        __m256 result = _mm256_add_ps(dstVec, srcVec);
        _mm256_storeu_ps(&dst[i], result);
    }

    // Обрабатываем по 4 семпла за раз (SSE)
    for (; i + 3 < count; i += 4) {
        __m128 dstVec = _mm_loadu_ps(&dst[i]);
        __m128 srcVec = _mm_loadu_ps(&src[i]);
        __m128 result = _mm_add_ps(dstVec, srcVec);
        _mm_storeu_ps(&dst[i], result);
    }

    // Оставшиеся семплы
    for (; i < count; i++) {
        dst[i] += src[i];
    }
}
#endif

// Базовая функция сложения (без SIMD)
void addSamplesBasic(float* dst, const float* src, size_t count) {
    for (size_t i = 0; i < count; i++) {
        dst[i] += src[i];
    }
}

// Векторизованная функция сложения
void addSamples(float* dst, const float* src, size_t count) {
#if USE_SIMD
    addSamplesSIMD(dst, src, count);
#else
    addSamplesBasic(dst, src, count);
#endif
}

// Конвертация байтов в float
vector<float> convertToFloat(const void* data, size_t byteCount,
                             int bitsPerSample, int channels, bool isSigned) {
    if (!data || byteCount == 0) {
        return {};
    }

    size_t sampleCount = byteCount / (bitsPerSample / 8);
    if (sampleCount == 0) {
        return {};
    }

    vector<float> result;
    result.reserve(sampleCount);

    if (bitsPerSample == 16 && isSigned) {
        const int16_t* src = static_cast<const int16_t*>(data);
        const float scale = 1.0f / 32768.0f;

        for (size_t i = 0; i < sampleCount; i++) {
            result.push_back(src[i] * scale);
        }
    }
    else if (bitsPerSample == 32) {
        const float* src = static_cast<const float*>(data);
        result.assign(src, src + sampleCount);
    }
    else if (bitsPerSample == 8 && !isSigned) {
        const uint8_t* src = static_cast<const uint8_t*>(data);
        const float scale = 1.0f / 128.0f;
        const float offset = -1.0f;

        for (size_t i = 0; i < sampleCount; i++) {
            result.push_back(src[i] * scale + offset);
        }
    }
    else if (bitsPerSample == 24) {
        // Поддержка 24-bit формата
        const uint8_t* src = static_cast<const uint8_t*>(data);
        const float scale = 1.0f / 8388608.0f; // 2^23

        for (size_t i = 0; i < sampleCount; i++) {
            int32_t sample = 0;
            // Little endian (наиболее распространенный)
            sample = (src[i * 3] << 8) | (src[i * 3 + 1] << 16) | (src[i * 3 + 2] << 24);
            sample >>= 8; // Приводим к signed 24-bit

            result.push_back(sample * scale);
        }
    }

    return result;
}

// Конвертация float в выходной формат
vector<uint8_t> convertFromFloat(const vector<float>& samples,
                                 int bitsPerSample, bool isSigned) {
    if (samples.empty()) {
        return {};
    }

    size_t sampleCount = samples.size();
    vector<uint8_t> result(sampleCount * (bitsPerSample / 8));

    if (bitsPerSample == 16 && isSigned) {
        int16_t* dst = reinterpret_cast<int16_t*>(result.data());
        const float scale = 32767.0f;

        for (size_t i = 0; i < sampleCount; i++) {
            float clipped = max(-1.0f, min(1.0f, samples[i]));
            dst[i] = static_cast<int16_t>(clipped * scale);
        }
    }
    else if (bitsPerSample == 32) {
        float* dst = reinterpret_cast<float*>(result.data());
        copy(samples.begin(), samples.end(), dst);
    }
    else if (bitsPerSample == 8 && !isSigned) {
        uint8_t* dst = result.data();
        const float scale = 128.0f;
        const float offset = 128.0f;

        for (size_t i = 0; i < sampleCount; i++) {
            float clipped = max(-1.0f, min(1.0f, samples[i]));
            dst[i] = static_cast<uint8_t>(clipped * scale + offset);
        }
    }

    return result;
}

// Оптимизированный ресамплинг с линейной интерполяцией
// Исправленный ресамплинг с линейной интерполяцией
vector<float> resample(const vector<float>& input, int inputRate,
                       int outputRate, int channels) {
    if (inputRate == outputRate || input.empty()) {
        return input;
    }

    // Проверка параметров
    if (inputRate <= 0 || outputRate <= 0 || channels <= 0) {
        return {};
    }

    size_t inputFrames = input.size() / channels;
    if (inputFrames == 0 || input.size() % channels != 0) {
        return {};
    }

    // Более точный расчет соотношения и выходной длины
    double ratio = static_cast<double>(inputRate) / outputRate;

    // Правильный расчет выходной длины: выходная_длина = входная_длина * (вых_частота / вход_частота)
    size_t outputFrames = static_cast<size_t>(
        ceil(static_cast<double>(inputFrames) * outputRate / inputRate));

    if (outputFrames == 0) {
        outputFrames = 1;
    }

    vector<float> result(outputFrames * channels);

    // Обработка моно
    if (channels == 1) {
        for (size_t i = 0; i < outputFrames; i++) {
            // Важно: используем double для точности при больших значениях
            double pos = i * ratio;

            // Граничные случаи
            if (pos >= inputFrames - 1) {
                result[i] = input[inputFrames - 1];
                continue;
            }

            size_t idx1 = static_cast<size_t>(floor(pos));
            size_t idx2 = idx1 + 1;

            if (idx2 >= inputFrames) {
                idx2 = inputFrames - 1;
            }

            double frac = pos - idx1;
            double sample1 = input[idx1];
            double sample2 = input[idx2];

            result[i] = static_cast<float>(sample1 * (1.0 - frac) + sample2 * frac);
        }
    }
    // Обработка стерео
    else if (channels == 2) {
        for (size_t i = 0; i < outputFrames; i++) {
            double pos = i * ratio;

            if (pos >= inputFrames - 1) {
                size_t lastFrame = inputFrames - 1;
                result[i * 2] = input[lastFrame * 2];
                result[i * 2 + 1] = input[lastFrame * 2 + 1];
                continue;
            }

            size_t idx1 = static_cast<size_t>(floor(pos));
            size_t idx2 = idx1 + 1;

            if (idx2 >= inputFrames) {
                idx2 = inputFrames - 1;
            }

            double frac = pos - idx1;
            double invFrac = 1.0 - frac;

            size_t inIdx1 = idx1 * 2;
            size_t inIdx2 = idx2 * 2;

            result[i * 2] = static_cast<float>(
                input[inIdx1] * invFrac + input[inIdx2] * frac);
            result[i * 2 + 1] = static_cast<float>(
                input[inIdx1 + 1] * invFrac + input[inIdx2 + 1] * frac);
        }
    }
    // Общий случай
    else {
        for (size_t i = 0; i < outputFrames; i++) {
            double pos = i * ratio;

            if (pos >= inputFrames - 1) {
                size_t lastFrame = inputFrames - 1;
                for (int ch = 0; ch < channels; ch++) {
                    result[i * channels + ch] = input[lastFrame * channels + ch];
                }
                continue;
            }

            size_t idx1 = static_cast<size_t>(floor(pos));
            size_t idx2 = idx1 + 1;

            if (idx2 >= inputFrames) {
                idx2 = inputFrames - 1;
            }

            double frac = pos - idx1;
            double invFrac = 1.0 - frac;

            for (int ch = 0; ch < channels; ch++) {
                result[i * channels + ch] = static_cast<float>(
                    input[idx1 * channels + ch] * invFrac +
                    input[idx2 * channels + ch] * frac);
            }
        }
    }

    return result;
}

// Конвертация каналов
vector<float> convertChannels(const vector<float>& input,
                             int inputChannels, int outputChannels) {
    if (inputChannels == outputChannels || input.empty()) {
        return input;
    }

    size_t inputFrames = input.size() / inputChannels;
    vector<float> result(inputFrames * outputChannels);

    if (inputChannels == 1 && outputChannels == 2) {
        // Моно -> стерео
        for (size_t i = 0; i < inputFrames; i++) {
            result[i * 2] = input[i];
            result[i * 2 + 1] = input[i];
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

// Находит максимальную длину среди всех треков
size_t findMaxFrames(const vector<AudioData>& tracks,
                     int outputChannels, int outputSampleRate) {
    size_t maxFrames = 0;

    for (const auto& track : tracks) {
        size_t trackFrames = track.frameCount;

        // Учитываем ресамплинг при определении длины
        if (track.sampleRate != outputSampleRate) {
            float ratio = static_cast<float>(track.sampleRate) / outputSampleRate;
            trackFrames = static_cast<size_t>(trackFrames / ratio + 0.5f);
        }

        maxFrames = max(maxFrames, trackFrames);
    }

    return maxFrames;
}

// Основная функция микширования
AudioData mixTracks(const vector<AudioData>& tracks,
                   int outputChannels, int outputSampleRate) {
    // Проверка параметров
    if (outputChannels <= 0 || outputSampleRate <= 0) {
        return AudioData();
    }

    if (tracks.empty()) {
        return AudioData();
    }

    // Находим максимальную длину
    size_t maxFrames = findMaxFrames(tracks, outputChannels, outputSampleRate);

    if (maxFrames == 0) {
        return AudioData();
    }

    size_t totalSamples = maxFrames * outputChannels;

    AudioData result;
    result.channels = outputChannels;
    result.sampleRate = outputSampleRate;
    result.frameCount = maxFrames;
    result.samples.resize(totalSamples, 0.0f);

    // Микшируем все треки
    for (const auto& track : tracks) {
        if (track.samples.empty()) {
            continue;
        }

        vector<float> processed = track.samples;

        // Ресамплинг если нужно
        if (track.sampleRate != outputSampleRate) {
            processed = resample(processed, track.sampleRate,
                                outputSampleRate, track.channels);
        }

        // Конвертация каналов если нужно
        if (track.channels != outputChannels) {
            processed = convertChannels(processed, track.channels, outputChannels);
        }

        // Добавляем к результату
        size_t samplesToMix = min(processed.size(), totalSamples);
        addSamples(result.samples.data(), processed.data(), samplesToMix);
    }

    // Нормализация с умягчением (soft knee)
    float maxSample = 0.001f; // начальное значение для избежания деления на 0

    // Находим максимальное абсолютное значение
    for (float sample : result.samples) {
        float absSample = fabsf(sample);
        if (absSample > maxSample) {
            maxSample = absSample;
        }
    }

    // Применяем умягчение если необходимо
    if (maxSample > 1.0f) {
        float threshold = 0.9f;
        float kneeWidth = 0.1f;

        if (maxSample > threshold + kneeWidth) {
            // Жесткое ограничение для сильного перегруза
            float gain = threshold / maxSample;
            for (float& sample : result.samples) {
                sample *= gain;
            }
        }
        else if (maxSample > threshold - kneeWidth) {
            // Умягчение в зоне knee
            float kneeStart = threshold - kneeWidth;
            float kneeEnd = threshold + kneeWidth;

            for (float& sample : result.samples) {
                float absSample = fabsf(sample);
                if (absSample > kneeStart) {
                    float x = (absSample - kneeStart) / (2.0f * kneeWidth);
                    float compression = 1.0f - (1.0f - threshold / maxSample) * x;
                    sample *= compression;
                }
            }
        }
        // Если maxSample <= 1.0f, нормализация не нужна
    }

    return result;
}

// Создание Java Track объекта
jobject createJavaTrack(JNIEnv* env, const vector<uint8_t>& data,
                       jobject outputFormat, long durationMillis) {
    if (!gCache.initialized.load()) {
        initCache(env);
    }

    JNILocalFrame frame(env, 8); // Создаем локальный фрейм для автоматического очищения

    try {
        // 1. Создаем ByteBuffer
        jobject byteBuffer = env->NewDirectByteBuffer(
            const_cast<uint8_t*>(data.data()),
            static_cast<jlong>(data.size())
        );

        if (!byteBuffer) {
            throwException(env, "Failed to create ByteBuffer");
            return nullptr;
        }

        // 2. Создаем Duration
        jobject duration = env->CallStaticObjectMethod(
            gCache.durationClass, gCache.durationOfMillis,
            static_cast<jlong>(durationMillis));

        if (!duration) {
            throwException(env, "Failed to create Duration");
            return nullptr;
        }

        // 3. Создаем TrackMetadata
        jobject metadata = env->NewObject(gCache.metadataClass,
            env->GetMethodID(gCache.metadataClass, "<init>", "()V"));

        if (!metadata) {
            throwException(env, "Failed to create TrackMetadata");
            return nullptr;
        }

        // 4. Создаем Track
        jobject track = env->NewObject(gCache.trackClass, gCache.trackConstructor,
            byteBuffer, duration, outputFormat, metadata);

        // Объекты будут автоматически удалены при выходе из фрейма
        return track ? env->NewLocalRef(track) : nullptr;

    } catch (...) {
        throwException(env, "Failed to create Java Track object");
        return nullptr;
    }
}

extern "C" {
    JNIEXPORT jobject JNICALL Java_org_plovdev_audioengine_mixer_NativeTrackMixer__1doMixing(
        JNIEnv* env, jobject jobj, jobject mixingTracks, jobject outputFormat) {

        // Инициализируем кэш если нужно
        if (!gCache.initialized.load()) {
            initCache(env);
            if (!gCache.initialized.load()) {
                throwException(env, "Failed to initialize JNI cache");
                return nullptr;
            }
        }

        // Используем RAII для управления локальными ссылками
        JNILocalFrame frame(env, 64);

        try {
            // 1. Получаем параметры выходного формата
            int outputChannels = env->CallIntMethod(outputFormat, gCache.formatChannels);
            int outputSampleRate = env->CallIntMethod(outputFormat, gCache.formatSampleRate);
            int outputBitsPerSample = env->CallIntMethod(outputFormat, gCache.formatBits);
            bool outputSigned = env->CallBooleanMethod(outputFormat, gCache.formatSigned);

            // Проверка параметров
            if (outputChannels <= 0 || outputSampleRate <= 0 ||
                (outputBitsPerSample != 8 && outputBitsPerSample != 16 &&
                 outputBitsPerSample != 24 && outputBitsPerSample != 32)) {
                throwException(env, "Invalid output format parameters");
                return nullptr;
            }

            // 2. Получаем список треков
            jint trackCount = env->CallIntMethod(mixingTracks, gCache.listSize);

            if (trackCount == 0) {
                throwException(env, "Track list is empty");
                return nullptr;
            }

            // 3. Собираем данные всех треков
            vector<AudioData> tracks;
            tracks.reserve(trackCount);

            for (jint i = 0; i < trackCount; i++) {
                jobject track = env->CallObjectMethod(mixingTracks, gCache.listGet, i);
                if (!track) {
                    continue;
                }

                // Получаем ByteBuffer с данными
                jobject byteBuffer = env->CallObjectMethod(track, gCache.trackGetData);
                if (!byteBuffer) {
                    env->DeleteLocalRef(track);
                    continue;
                }

                void* bufferPtr = env->GetDirectBufferAddress(byteBuffer);
                jlong bufferSize = env->GetDirectBufferCapacity(byteBuffer);

                if (!bufferPtr || bufferSize <= 0) {
                    env->DeleteLocalRef(byteBuffer);
                    env->DeleteLocalRef(track);
                    continue;
                }

                // Получаем формат трека
                jobject trackFormat = env->CallObjectMethod(track, gCache.trackGetFormat);
                if (!trackFormat) {
                    env->DeleteLocalRef(byteBuffer);
                    env->DeleteLocalRef(track);
                    continue;
                }

                int channels = env->CallIntMethod(trackFormat, gCache.formatChannels);
                int sampleRate = env->CallIntMethod(trackFormat, gCache.formatSampleRate);
                int bitsPerSample = env->CallIntMethod(trackFormat, gCache.formatBits);
                bool isSigned = env->CallBooleanMethod(trackFormat, gCache.formatSigned);

                // Проверка формата трека
                if (channels <= 0 || sampleRate <= 0 ||
                    (bitsPerSample != 8 && bitsPerSample != 16 &&
                     bitsPerSample != 24 && bitsPerSample != 32)) {
                    env->DeleteLocalRef(trackFormat);
                    env->DeleteLocalRef(byteBuffer);
                    env->DeleteLocalRef(track);
                    continue;
                }

                // Конвертируем в float
                vector<float> floatSamples = convertToFloat(
                    bufferPtr, static_cast<size_t>(bufferSize),
                    bitsPerSample, channels, isSigned);

                if (floatSamples.empty()) {
                    env->DeleteLocalRef(trackFormat);
                    env->DeleteLocalRef(byteBuffer);
                    env->DeleteLocalRef(track);
                    continue;
                }

                AudioData audioData;
                audioData.samples = move(floatSamples);
                audioData.channels = channels;
                audioData.sampleRate = sampleRate;
                audioData.frameCount = audioData.samples.size() / channels;

                tracks.push_back(move(audioData));

                // Очищаем локальные ссылки (автоматически при выходе из фрейма)
                env->DeleteLocalRef(trackFormat);
                env->DeleteLocalRef(byteBuffer);
                env->DeleteLocalRef(track);
            }

            if (tracks.empty()) {
                throwException(env, "No valid tracks to mix");
                return nullptr;
            }

            // 4. Микшируем треки
            AudioData mixed = mixTracks(tracks, outputChannels, outputSampleRate);

            if (mixed.samples.empty()) {
                throwException(env, "Mixing produced empty result");
                return nullptr;
            }

            // 5. Конвертируем обратно в выходной формат
            vector<uint8_t> outputData = convertFromFloat(
                mixed.samples, outputBitsPerSample, outputSigned);

            if (outputData.empty()) {
                throwException(env, "Failed to convert mixed data to output format");
                return nullptr;
            }

            // 6. Рассчитываем длительность результата
            long durationMillis = static_cast<long>(
                (mixed.frameCount * 1000L) / outputSampleRate);

            // 7. Создаем Java Track объект
            jobject result = createJavaTrack(env, outputData, outputFormat, durationMillis);

            if (!result) {
                throwException(env, "Failed to create result Track object");
                return nullptr;
            }

            return env->NewLocalRef(result);

        } catch (const bad_alloc& e) {
            throwException(env, "Memory allocation failed during mixing");
            return nullptr;
        } catch (const exception& e) {
            throwException(env, e.what());
            return nullptr;
        } catch (...) {
            throwException(env, "Unknown error during mixing");
            return nullptr;
        }
    }
}