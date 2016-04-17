package com.zenika.poc.share;

import org.junit.Test;

import static com.zenika.poc.share.Events.singletonEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

public class EventsTest {

    @Test
    public void should_not_create_event_with_different_aggregate_id() {
        // When
        Throwable throwable = catchThrowable(() -> new Events<>(new TestEvent("id_1"), new TestEvent("id_2")));

        // Then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_accept_event_with_different_aggregate_id() {
        // Given
        Events<TestEvent> events = singletonEvents(new TestEvent("id"));

        // When
        Throwable throwable = catchThrowable(() -> events.add(new TestEvent("other_id")));

        // Then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    private static class TestEvent extends Event {

        public TestEvent(String aggregateId) {
            super(aggregateId);
        }

        @Override
        public String eventType() {
            return "test";
        }
    }

}