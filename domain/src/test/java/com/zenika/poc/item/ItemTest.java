package com.zenika.poc.item;

import com.zenika.poc.item.exception.InvalidItemNameException;
import com.zenika.poc.item.exception.NegativeItemNumberException;
import com.zenika.poc.item.exception.NegativePriceException;
import com.zenika.poc.item.exception.UnknownPendingReservationException;
import com.zenika.poc.share.Events;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

public class ItemTest {

    public static final Item COFFEE = Item.createStock("coffee", 10, 1);

    @Test
    public void should_create_stock() {
        // Given When 
        Item item = Item.createStock("coffee", 10, 1);

        // Then
        assertThat(item).isEqualTo(Item.createStock("coffee", 10, 1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }

    @Test
    public void should_add_stock() {
        // Given When
        Item item = COFFEE.add(10);

        // Then
        assertThat(item).isEqualTo(Item.createStock("coffee", 20, 1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }

    @Test
    public void should_fail_when_adding_negative_stock() {
        // Given When
        Throwable throwable = catchThrowable(() -> COFFEE.add(-1));

        // Then
        assertThat(throwable).isInstanceOf(NegativeItemNumberException.class);
    }

    @Test
    public void should_change_price() {
        // Given When
        Item item = COFFEE.changePrice(1.1);

        // Then
        assertThat(item).isEqualTo(Item.createStock("coffee", 10, 1.1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }

    @Test
    public void should_when_setting_negative_price() {
        // Given When
        Throwable throwable = catchThrowable(() -> COFFEE.changePrice(-1));

        // Then
        assertThat(throwable).isInstanceOf(NegativePriceException.class);
    }

    @Test
    public void should_change_name() {
        // Given When
        Item item = COFFEE.changeName("irish coffee");

        // Then
        assertThat(item).isEqualTo(Item.createStock("irish coffee", 10, 1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }

    @Test
    public void should_fail_when_changing_name_to_empty() {
        // Given When
        Throwable throwable = catchThrowable(() -> COFFEE.changeName(""));

        // Then
        assertThat(throwable).isInstanceOf(InvalidItemNameException.class);
    }

    @Test
    public void should_reserve_items() {
        // Given When
        Item item = COFFEE.reserve(5).item;

        // Then
        assertThat(item).isEqualTo(Item.createStock("coffee", 5, 1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }

    @Test
    public void should_return_last_reservation_id_created() {
        // Given When
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        String reservationId = reservationResult.reservationId;

        // Then
        assertThat(reservationId).isNotNull();
    }

    @Test
    public void should_confirm_item_reservation() {
        // Given
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        Item item = reservationResult.item;
        String reservationId = reservationResult.reservationId;

        // When
        item = item.confirmReservation(reservationId);

        // Then
        assertThat(item).isEqualTo(Item.createStock("coffee", 5, 1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }

    @Test
    public void should_fail_when_confirming_already_confirmed_reservation() {
        // Given
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        Item item = reservationResult.item;
        String reservationId = reservationResult.reservationId;

        // When
        Throwable throwable = catchThrowable(() -> item.confirmReservation(reservationId)
                                                       .confirmReservation(reservationId));

        // Then
        assertThat(throwable).isInstanceOf(UnknownPendingReservationException.class);
    }

    @Test
    public void should_fail_when_confirming_canceled_reservation() {
        // Given
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        Item item = reservationResult.item;
        String reservationId = reservationResult.reservationId;

        // When
        Throwable throwable = catchThrowable(() -> item.cancelReservation(reservationId)
                                                       .confirmReservation(reservationId));

        // Then
        assertThat(throwable).isInstanceOf(UnknownPendingReservationException.class);
    }

    @Test
    public void should_fail_when_confirming_unknown_reservation() {
        // Given When
        Throwable throwable = catchThrowable(() -> COFFEE.confirmReservation(""));

        // Then
        assertThat(throwable).isInstanceOf(UnknownPendingReservationException.class);
    }

    @Test
    public void should_fail_when_canceling_unknown_reservation() {
        // Given
        // When
        Throwable throwable = catchThrowable(() -> COFFEE.confirmReservation(""));

        // Then
        assertThat(throwable).isInstanceOf(UnknownPendingReservationException.class);
    }

    @Test
    public void should_cancel_reservation() {
        // Given
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        Item item = reservationResult.item;
        String reservationId = reservationResult.reservationId;

        // When
        item = item.add(10).cancelReservation(reservationId);

        // Then
        assertThat(item).isEqualTo(Item.createStock("coffee", 20, 1));
        assertThat(item).isEqualTo(Item.loadStock(doubleEvents(item.events())));
    }


    @Test
    public void should_fail_when_canceling_already_confirmed_reservation() {
        // Given
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        Item item = reservationResult.item;
        String reservationId = reservationResult.reservationId;

        // When
        Throwable throwable = catchThrowable(() -> item.confirmReservation(reservationId)
                                                       .cancelReservation(reservationId));

        // Then
        assertThat(throwable).isInstanceOf(UnknownPendingReservationException.class);
    }

    @Test
    public void should_fail_when_canceling_already_canceled_reservation() {
        // Given
        Item.ReservationResult reservationResult = COFFEE.reserve(5);
        Item item = reservationResult.item;
        String reservationId = reservationResult.reservationId;

        // When
        Throwable throwable = catchThrowable(() -> item.cancelReservation(reservationId)
                                                       .cancelReservation(reservationId));

        // Then
        assertThat(throwable).isInstanceOf(UnknownPendingReservationException.class);
    }

    private Events<ItemEvent> doubleEvents(Events<ItemEvent> events) {
        return new Events<>(events.stream()
                                  .flatMap(event -> asList(event, event).stream())
                                  .collect(toList()));
    }

}