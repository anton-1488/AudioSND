package org.plovdev.audioengine.loaders;

public abstract class LoadListenerAdapter implements LoadListener {
    /**
     * Вызывается когда начинается загрузка
     *
     * @param total общий размер в байтах (если известен)
     */
    @Override
    public void onLoadStarted(long total) {

    }

    /**
     * Вызывается во время загрузки
     *
     * @param loaded уже загружено байт
     */
    @Override
    public void onLoading(long loaded) {

    }

    /**
     * Вызывается когда загрузка завершена успешно
     */
    @Override
    public void onLoadFinished() {

    }

    /**
     * Вызывается при ошибке загрузки
     *
     * @param error исключение
     */
    @Override
    public void onLoadFailed(Exception error) {

    }
}
