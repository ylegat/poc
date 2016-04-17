package com.zenika.poc.share;

public abstract class Aggregate<AGGREGATE extends Aggregate<AGGREGATE, EVENT>, EVENT extends Event> {

    public final String id;

    protected final Events<EVENT> events;

    public Aggregate(String id, Events<EVENT> events) {
        this.id = id;
        this.events = events;
    }

    protected AGGREGATE applyEvents(Events<EVENT> events) {
        Aggregate<AGGREGATE, EVENT> current = this;
        for (EVENT event : events) {
            current = current.applyEvent(event);
        }

        return (AGGREGATE) current;
    }

    protected abstract AGGREGATE applyEvent(EVENT event);

    public Events<EVENT> events() {
        return events;
    }

    public Events<EVENT> eventsFrom(int version) {
        return events.eventsFrom(version);
    }

}
