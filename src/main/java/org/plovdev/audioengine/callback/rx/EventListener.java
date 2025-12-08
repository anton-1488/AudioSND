package org.plovdev.audioengine.callback.rx;

public interface EventListener {
    void onEvent(EventsChannel channel);
    ChannelType getChanelType();
}