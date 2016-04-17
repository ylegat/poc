package com.zenika.poc.share;

public interface EventRepository<EVENT extends Event> {

    Events<EVENT> events(String aggregateId);

    void addEvents(String aggregateId, Events<EVENT> events);

    void clearAllEvents();

}
