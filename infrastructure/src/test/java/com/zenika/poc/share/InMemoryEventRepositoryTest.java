package com.zenika.poc.share;

import org.junit.Test;

import static com.zenika.poc.share.Events.singletonEvents;
import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryEventRepositoryTest {

    private static class EventTest extends Event {

        public EventTest(String aggregateId) {
            super(aggregateId);
        }

        @Override
        public String eventType() {
            return "test";
        }
    }
    
    @Test
    public void should_insert_events() {
        // Given
        InMemoryEventRepository<EventTest> repository = new InMemoryEventRepository<>();
        Events<EventTest> events = singletonEvents(new EventTest("id"));

        // When
        repository.addEvents(events);

        // Then
        assertThat(repository.events("id")).isEqualTo(events);
    }

    @Test
    public void should_insert_new_events() {
        // Given
        InMemoryEventRepository<EventTest> repository = new InMemoryEventRepository<>();
        EventTest event1 = new EventTest("id");
        EventTest event2 = new EventTest("id");
        repository.addEvents(singletonEvents(event1));

        // When
        repository.addEvents(singletonEvents(event2));


        // Then
        assertThat(repository.events("id")).containsExactly(event1, event2);
    }

}