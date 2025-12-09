package org.plovdev.audioengine.mixer;

import org.plovdev.audioengine.exceptions.MixingException;
import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.util.List;

public interface TrackMixer {
    /**
     * Установить выходной формат микшера
     */
    void setOutputFormat(TrackFormat format);

    /**
     * Получить текущий выходной формат
     */
    TrackFormat getOutputFormat();

    // ==== Управление треками ====

    /**
     * Добавить трек в микшер
     */
    void addTrack(Track track);

    /**
     * Добавить трек с каналом
     */
    void addTrack(Track track, int channel);

    /**
     * Удалить трек из микшера
     */
    void removeTrack(Track track);

    /**
     * Получить все треки в микшере
     */
    List<Track> getMixingTracks();

    /**
     * Очистить все треки
     */
    void clearTracks();

    // ==== Микширование ====

    /**
     * Выполнить микширование всех треков
     */
    Track doMixing() throws MixingException;
    /**
     * Проверить, пуст ли микшер
     */
    boolean isEmpty();

    /**
     * Получить количество треков
     */
    int getTrackCount();
}