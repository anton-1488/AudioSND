package org.plovdev.audioengine.callback.rx;

/**
 * Enum of different EventChannel types.
 *
 * @see EventManager
 * @see EventListener
 * @see EventsChannel
 *
 * @author Anton
 * @version 1.0
 */
public enum ChannelType {
    MIXER_CHANNEL_ADDED,     // Chanell has been added to the mixer.
    MIXER_CHANNEL_REMOVED,   // Chanell has been added to the mixer.

    MIXER_VOLUME_CHANGED,    // Volume has been changed

    EFFECT_APPLIED,          // Applied effect

    DEVICE_CHANGED,          // Audio device has been changed(user connect other speakers to PC)
}