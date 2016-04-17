package com.zenika.poc.share;

import java.util.Objects;

public abstract class Aggregate<AGGREGATE extends Aggregate<AGGREGATE, EVENT>, EVENT extends Event> {

    public String id;

    protected Events<EVENT> events;

    public Aggregate(String id, Events<EVENT> events) {
        this.id = id;
        this.events = events;
    }

    protected void applyEvents(Events<EVENT> events) {
        Aggregate<AGGREGATE, EVENT> current = this;
        for (EVENT event : events) {
            current.applyEvent(event);
        }
    }

    protected abstract void applyEvent(EVENT event);

    public Events<EVENT> events() {
        return events;
    }

    public Events<EVENT> eventsFrom(int version) {
        return events.eventsFrom(version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
