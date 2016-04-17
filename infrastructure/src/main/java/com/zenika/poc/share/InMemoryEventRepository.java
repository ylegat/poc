package com.zenika.poc.share;

import java.util.HashMap;
import java.util.Map;

import static com.zenika.poc.share.Events.emptyEvents;

public class InMemoryEventRepository<EVENT extends Event> implements EventRepository<EVENT> {

    private final Map<String, Events<EVENT>> events;

    private final Object lock;

    public InMemoryEventRepository() {
        events = new HashMap<>();
        lock = new Object();
    }

    @Override
    public Events<EVENT> events(String aggregateId) {
        synchronized (lock) {
            return events.get(aggregateId);
        }
    }

    @Override
    public void addEvents(Events<EVENT> events) {
        synchronized (lock) {
            Events<EVENT> currentEvents = this.events.get(events.aggregateId());
            if (currentEvents == null) {
                currentEvents = emptyEvents();
                this.events.put(events.aggregateId(), currentEvents);
            }

            currentEvents.addAll(events);
        }
    }

    @Override
    public void clearAllEvents() {
        events.clear();
    }

}
