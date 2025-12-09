package org.plovdev.audioengine.callback.rx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager {
    private static EventManager instanse = null;
    private final ExecutorService broadcastExecutor = Executors.newCachedThreadPool();
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public static EventManager getInstance() {
        if (instanse == null) instanse = new EventManager();
        return instanse;
    }

    private EventManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(broadcastExecutor::shutdownNow));
    }

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    public void broadcast(EventsChannel channel) {
        for (EventListener listener : listeners) {
            if (listener.getChanelType() == channel.getChannelType()) {
                broadcastExecutor.submit(() -> {
                    try {
                        listener.onEvent(channel);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    return listener;
                });
            }
        }
    }
}