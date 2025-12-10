package org.plovdev.audioengine.callback.rx;

/**
 * Event-driven contract architecture
 *
 * @author Anton
 * @version 1.0
 */
public interface EventListener {
    void onEvent(EventsChannel channel);
    ChannelType getChanelType();
}