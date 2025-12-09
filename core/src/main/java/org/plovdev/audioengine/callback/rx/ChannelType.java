package org.plovdev.audioengine.callback.rx;

public enum ChannelType {
    TRACK_PLAY_STARTED, TRACK_PLAY_PAUSED, TRACK_PLAY_STOPPED,

    TRACK_LOAD_STARTED,      // Началась загрузка трека
    TRACK_LOAD_PROGRESS,     // Прогресс загрузки
    TRACK_LOAD_COMPLETED,    // Загрузка завершена
    TRACK_LOAD_FAILED,       // Ошибка загрузки

    MIXER_CHANNEL_ADDED,     // Добавлен канал в микшер
    MIXER_CHANNEL_REMOVED,   // Удален канал из микшера
    MIXER_VOLUME_CHANGED,    // Изменена громкость

    EFFECT_APPLIED,          // Применен эффект
    EFFECT_CHAIN_UPDATED,    // Обновлена цепочка эффектов

    DEVICE_CHANGED,          // Изменилось аудио-устройство
    BUFFER_UNDERRUN,         // Проблемы с буфером (задержки)
}