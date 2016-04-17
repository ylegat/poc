package com.zenika.poc.share;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Events<EVENT extends Event> implements Iterable<EVENT> {

    public static <EVENT extends Event> Events<EVENT> emptyEvents() {
        return new Events<>();
    }

    public static <EVENT extends Event> Events<EVENT> singletonEvents(EVENT event) {
        return new Events<>(event);
    }

    private final List<EVENT> events;

    private Events() {
        this(emptyList());
    }

    public Events(EVENT event) {
        this(singletonList(event));
    }

    public Events(EVENT... events) {
        this(asList(events));
    }

    public Events(List<EVENT> events) {
        long distinctAggregateId = events.stream()
                                         .map(event -> event.aggregateId)
                                         .distinct()
                                         .count();

        checkArgument(distinctAggregateId == 0 || distinctAggregateId == 1, "Only event associated to the same aggregate can be grouped");
        this.events = events;
    }

    public Events<EVENT> add(EVENT event) {
        return add(singletonEvents(event));
    }

    public Events<EVENT> add(Events<EVENT> events) {
        List<EVENT> copy = new ArrayList<>(this.events);
        copy.addAll(events.events);
        return new Events<>(copy);
    }

    public Stream<EVENT> stream() {
        return events.stream();
    }

    @Override
    public Iterator<EVENT> iterator() {
        return events.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Events)) return false;
        Events<?> that = (Events<?>) o;
        return Objects.equals(this.events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }
}
