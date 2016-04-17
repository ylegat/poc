package com.zenika.poc.share;

import java.util.Objects;

public abstract class Event {

    public final String aggregateId;

    public Event(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(aggregateId, event.aggregateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId);
    }

    public abstract String eventType();

}
