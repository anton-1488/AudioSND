package org.plovdev.audioengine.utils;

public interface LoadListener {

    /**
     * Вызывается когда начинается загрузка
     * @param total общий размер в байтах (если известен)
     */
    void onLoadStarted(long total);

    /**
     * Вызывается во время загрузки
     * @param loaded уже загружено байт
     */
    void onLoading(long loaded);

    /**
     * Вызывается когда загрузка завершена успешно
     */
    void onLoadFinished();

    /**
     * Вызывается при ошибке загрузки
     * @param error исключение
     */
    void onLoadFailed(Exception error);

    /**
     * Вызывается при отмене загрузки
     */
    void onLoadCancelled();
}