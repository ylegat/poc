package com.zenika.poc.share;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class InMemoryEventRepository<EVENT extends Event> implements EventRepository<EVENT> {

    private final Map<String, Events<EVENT>> events;

    private final Object lock;
    private Gson gson;

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
    public void addEvents(String aggregateId, Events<EVENT> events) {
        synchronized (lock) {
            this.events.computeIfPresent(aggregateId, (id, e) -> e.add(events));
            this.events.computeIfAbsent(aggregateId, (id) -> events);
        }
    }

    @Override
    public void clearAllEvents() {
        events.clear();
    }

}
