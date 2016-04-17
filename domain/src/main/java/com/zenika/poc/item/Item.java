package com.zenika.poc.item;

import com.google.common.base.Strings;
import com.zenika.poc.item.ItemEvent.ItemCreated;
import com.zenika.poc.item.exception.*;
import com.zenika.poc.share.Aggregate;
import com.zenika.poc.share.Events;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.zenika.poc.share.Events.emptyEvents;
import static com.zenika.poc.share.Events.singletonEvents;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;

public class Item extends Aggregate<Item, ItemEvent> {

    public static class ReservationResult {
        public final Item item;
        public final String reservationId;

        public ReservationResult(Item item, String reservationId) {
            this.item = item;
            this.reservationId = reservationId;
        }
    }

    private static final Item INIT_ITEM = new Item(null, emptyEvents(), "INIT", 0, 0, emptySet());

    public static Item createStock(String name, int stock, double price) {
        String stockId = randomUUID().toString();
        return new Item(stockId,
                        singletonEvents(new ItemCreated(stockId, name, stock, price)),
                        name,
                        stock,
                        price,
                        emptySet());
    }

    public static Item loadStock(Events<ItemEvent> events) {
        return INIT_ITEM.applyEvents(events);
    }

    public final String name;

    public final int stock;

    public final double price;

    public final Set<String> pendingReservation;

    private Item(String id, Events<ItemEvent> events, String name, int stock, double price, Set<String> pendingReservation) {
        super(id, events);
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.pendingReservation = pendingReservation;
    }

    @Override
    protected Item applyEvent(ItemEvent event) {
        Set<String> pendingReservationUpdated;
        switch (event.type) {
            case ITEM_CREATED:
                ItemCreated itemCreated = (ItemCreated) event;
                return new Item(itemCreated.aggregateId, singletonEvents(event), itemCreated.name, itemCreated.stock, itemCreated.price, pendingReservation);
            case ITEM_ADDED:
                ItemEvent.ItemAdded itemAdded = (ItemEvent.ItemAdded) event;
                return new Item(id, events.add(event), name, itemAdded.stock, price, pendingReservation);
            case ITEM_RESERVED:
                ItemEvent.ItemReserved itemReserved = (ItemEvent.ItemReserved) event;
                pendingReservationUpdated = new HashSet<>(pendingReservation);
                pendingReservationUpdated.add(itemReserved.reservationId);
                return new Item(id, events.add(event), name, itemReserved.stock, price, pendingReservationUpdated);
            case ITEM_RESERVATION_CONFIRMED:
                ItemEvent.ItemReservationConfirmed itemReservationConfirmed = (ItemEvent.ItemReservationConfirmed) event;
                pendingReservationUpdated = new HashSet<>(pendingReservation);
                pendingReservationUpdated.remove(itemReservationConfirmed.reservationId);
                return new Item(id, events.add(event), name, stock, price, pendingReservationUpdated);
            case ITEM_RESERVATION_CANCELED:
                ItemEvent.ItemReservationCanceled itemReservationCanceled = (ItemEvent.ItemReservationCanceled) event;
                pendingReservationUpdated = new HashSet<>(pendingReservation);
                pendingReservationUpdated.remove(itemReservationCanceled.reservationId);
                return new Item(id, events.add(event), name, itemReservationCanceled.stock, price, pendingReservationUpdated);
            case ITEM_PRICE_CHANGED:
                ItemEvent.ItemPriceChanged itemPriceChanged = (ItemEvent.ItemPriceChanged) event;
                return new Item(id, events.add(event), name, stock, itemPriceChanged.price, pendingReservation);
            case ITEM_NAME_CHANGED:
                ItemEvent.ItemNameChanged itemNameChanged = (ItemEvent.ItemNameChanged) event;
                return new Item(id, events.add(event), itemNameChanged.name, stock, price, pendingReservation);
            default:
                throw new RuntimeException(format("Unexpected event type %s for event %s", event.type, event));
        }
    }

    public Item changeName(String name) {
        checkName(name);
        return applyEvent(new ItemEvent.ItemNameChanged(id, name));
    }

    public Item changePrice(double newPrice) {
        checkPrice(newPrice);
        return applyEvent(new ItemEvent.ItemPriceChanged(id, newPrice));
    }

    public Item add(int number) {
        checkNumberIsPositive(number);
        return applyEvent(new ItemEvent.ItemAdded(id, stock + number));
    }

    public ReservationResult reserve(int reservation) {
        checkNumberIsPositive(reservation);
        checkReservation(reservation);
        String reservationId = randomUUID().toString();
        return new ReservationResult(applyEvent(new ItemEvent.ItemReserved(id, reservationId, reservation, reservation)), reservationId);
    }

    public Item cancelReservation(String reservationId) {
        checkReservationIsPending(reservationId);
        ItemEvent.ItemReserved reservation = reservation(reservationId);
        return applyEvent(new ItemEvent.ItemReservationCanceled(id, reservationId, stock + reservation.reservation));
    }

    public Item confirmReservation(String reservationId) {
        checkReservationIsPending(reservationId);
        return applyEvent(new ItemEvent.ItemReservationConfirmed(id, reservationId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item that = (Item) o;
        return stock == that.stock &&
               Double.compare(that.price, price) == 0 &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private ItemEvent.ItemReserved reservation(String reservationId) {
        return events.stream()
                     .filter(event -> event.isType(ItemEvent.StockEventType.ITEM_RESERVED))
                     .map(event -> (ItemEvent.ItemReserved) event)
                     .filter(event -> event.isReservationId(reservationId))
                     .findFirst()
                     .get();
    }

    private void checkReservationIsPending(String reservationId) {
        if (!pendingReservation.contains(reservationId)) {
            throw new UnknownPendingReservationException();
        }
    }

    private void checkName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new InvalidItemNameException();
        }
    }

    private void checkPrice(double price) {
        if (price < 0) {
            throw new NegativePriceException();
        }
    }

    private void checkNumberIsPositive(int number) {
        if (number < 0) {
            throw new NegativeItemNumberException();
        }
    }

    private void checkReservation(int reservation) {
        if (reservation < 0) {
            throw new NegativeItemNumberException();
        }

        if (stock < reservation) {
            throw new WithdrawOutOfLimitException();
        }
    }

}
