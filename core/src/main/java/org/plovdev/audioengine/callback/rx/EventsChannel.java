package org.plovdev.audioengine.callback.rx;

public class EventsChannel {
    private ChannelType channelType;
    private Object eventData;

    public EventsChannel() {
    }

    public <T> EventsChannel(ChannelType channelType, T eventData) {
        this.channelType = channelType;
        this.eventData = eventData;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getEventData() {
        return (T) eventData;
    }

    public <T> void setEventData(T eventData) {
        this.eventData = eventData;
    }
}