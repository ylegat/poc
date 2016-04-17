package com.zenika.poc.item;

import com.zenika.poc.item.exception.InvalidItemNameException;
import com.zenika.poc.item.exception.NegativeItemNumberException;
import com.zenika.poc.item.exception.NegativePriceException;
import com.zenika.poc.item.exception.UnknownPendingReservationException;
import com.zenika.poc.share.Events;
import org.junit.Test;

import static com.zenika.poc.item.Item.createStock;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

public class ItemTest {

    @Test
    public void should_create_stock() {
        // Given When
        Item item = coffee();

        // Then
        assertThat(item).isEqualTo(createStock(item.id, "coffee", 10, 1));
    }

    @Test
    public void should_add_stock() {
        // Given When
        Item item = coffee();
        item.add(10);

        // Then
        assertThat(item).isEqualTo(createStock(item.id, "coffee", 20, 1));
    }

    @Test
    public void should_fail_when_adding_negative_stock() {
        // Given When
        Throwable throwable = catchThrowable(() -> coffee().add(-1));

        // Then
        assertThat(throwable).isInstanceOf(NegativeItemNumberException.class);
    }

    @Test
    public void should_change_price() {
        // Given When
        Item item = coffee();
        item.changePrice(1.1);

        // Then
        assertThat(item).isEqualTo(createStock(item.id, "coffee", 10, 1.1));
    }

    @Test
    public void should_when_setting_negative_price() {
        // Given When
        Throwable throwable = catchThrowable(() -> coffee().changePrice(-1));

        // Then
        assertThat(throwable).isInstanceOf(NegativePriceException.class);
    }

    @Test
    public void should_change_name() {
        // Given When
        Item item = coffee();
        item.changeName("irish coffee");

        // Then
        assertThat(item).isEqualTo(createStock(item.id, "irish coffee", 10, 1));
    }

    @Test
    public void should_fail_when_changing_name_to_empty() {
        // Given When
        Throwable throwable = catchThrowable(() -> coffee().changeName(""));

        // Then
        assertThat(throwable).isInstanceOf(InvalidItemNameException.class);
    }

    @Test
    public void should_remove_items() {
        // Given When
        Item item = coffee();
        item.remove(5);

        // Then
        assertThat(item).isEqualTo(createStock(item.id, "coffee", 5, 1));
    }

    @Test
    public void should_events_be_idempotent() {
        Item item = coffee();
        item.add(10);
        item.remove(5);
        item.changeName("irish coffee");
        item.changePrice(20);

        assertThat(Item.loadStock(doubleEvents(item.events()))).isEqualTo(item);
    }

    private Item coffee() {
        return createStock("coffee", 10, 1);
    }

    private Events<ItemEvent> doubleEvents(Events<ItemEvent> events) {
        return new Events<>(events.stream()
                                  .flatMap(event -> asList(event, event).stream())
                                  .collect(toList()));
    }

}