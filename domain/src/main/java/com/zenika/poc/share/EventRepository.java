package com.zenika.poc.share;

public interface EventRepository<EVENT extends Event> {

    Events<EVENT> events(String aggregateId);

    void addEvents(Events<EVENT> events);

    void clearAllEvents();

}
