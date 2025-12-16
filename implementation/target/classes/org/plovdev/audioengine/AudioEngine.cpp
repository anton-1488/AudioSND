#include <jni.h>
#include "org_plovdev_audioengine_NativeAudioEngine.h"

// Глобальные структуры движка (например, карты устройств)
static bool engineInitialized = false;

extern "C" {

/*
 * Class:     org_plovdev_audioengine_NativeAudioEngine
 * Method:    _init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_plovdev_audioengine_NativeAudioEngine__1init
  (JNIEnv* env, jobject obj) {
    if (engineInitialized) return;

    // TODO: здесь можно инициализировать глобальные структуры аудиоустройств,
    // создать пул потоков, буферы, синхронизацию и т.д.

    engineInitialized = true;
}

/*
 * Class:     org_plovdev_audioengine_NativeAudioEngine
 * Method:    _cleanup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_plovdev_audioengine_NativeAudioEngine__1cleanup
  (JNIEnv* env, jobject obj) {
    if (!engineInitialized) return;

    // TODO: освободить все глобальные ресурсы движка,
    // закрыть открытые устройства, очистить буферы

    engineInitialized = false;
}
}