package com.zenika.poc.share;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class EventDispatcher {

    private static final Object lock = new Object();

    private static final Multimap<String, EventConsumer> listeners = HashMultimap.create();

    public static void addListener(EventConsumer eventConsumer, String... eventTypes) {
        synchronized (lock) {
            for (String eventType : eventTypes) {
                listeners.put(eventType, eventConsumer);
            }
        }
    }

    public static void sendEvents(Events<?> events) {
        synchronized (lock) {
            for (Event event : events) {
                listeners.get(event.eventType())
                         .forEach(eventConsumer -> eventConsumer.consume(event));
            }
        }
    }
}
