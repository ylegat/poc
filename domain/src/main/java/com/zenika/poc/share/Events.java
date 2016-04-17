package com.zenika.poc.share;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

public class Events<EVENT extends Event> implements Iterable<EVENT> {

    public static <EVENT extends Event> Events<EVENT> emptyEvents() {
        return new Events<>();
    }

    public static <EVENT extends Event> Events<EVENT> singletonEvents(EVENT event) {
        return new Events<>(event);
    }

    private final List<EVENT> events;

    private String aggregateId;

    private Events() {
        this(new ArrayList<>());
    }

    public Events(EVENT... events) {
        this(newArrayList(events));
    }

    public Events(List<EVENT> events) {
        long distinctAggregateId = events.stream()
                                         .map(event -> event.aggregateId)
                                         .distinct()
                                         .count();

        checkArgument(distinctAggregateId == 0 || distinctAggregateId == 1,
                      "Only event associated to the same aggregate can be grouped");

        this.events = events;
        this.aggregateId = events.stream()
                                 .findFirst()
                                 .map(event -> event.aggregateId)
                                 .orElse(null);
    }

    public void add(EVENT event) {
        if (aggregateId == null) {
            aggregateId = event.aggregateId;
        } else {
            checkArgument(Objects.equals(aggregateId, event.aggregateId),
                          "Only event associated to the same aggregate can be grouped");
        }

        events.add(event);
    }

    public void addAll(Events<EVENT> events) {
        events.stream().forEach(this::add);
    }

    public Stream<EVENT> stream() {
        return events.stream();
    }

    public String aggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
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
