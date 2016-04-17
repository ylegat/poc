package com.zenika.poc.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.zenika.poc.item.ItemEvent.ItemAdded;
import com.zenika.poc.item.ItemEvent.ItemCreated;
import com.zenika.poc.item.ItemEvent.ItemPriceChanged;
import com.zenika.poc.item.ItemEvent.ItemReserved;
import com.zenika.poc.item.exception.*;
import com.zenika.poc.share.Aggregate;
import com.zenika.poc.share.Events;

import java.util.Objects;

import static com.zenika.poc.share.Events.emptyEvents;
import static com.zenika.poc.share.Events.singletonEvents;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class Item extends Aggregate<Item, ItemEvent> {

    @VisibleForTesting
    protected static Item createStock(String stockId, String name, int stock, double price) {
        return new Item(stockId,
                        singletonEvents(new ItemCreated(stockId, name, stock, price)),
                        name,
                        stock,
                        price
        );
    }

    public static Item createStock(String name, int stock, double price) {
        String stockId = randomUUID().toString();
        return createStock(stockId, name, stock, price);
    }

    public static Item loadStock(Events<ItemEvent> events) {
        Item item = new Item(null, emptyEvents(), "INIT", 0, 0);
        item.applyEvents(events);
        return item;
    }

    public String name;

    public int stock;

    public double price;

    private Item(String id, Events<ItemEvent> events, String name, int stock, double price) {
        super(id, events);
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    @Override
    protected void applyEvent(ItemEvent event) {
        switch (event.type) {
            case ITEM_CREATED:
                ItemCreated itemCreated = (ItemCreated) event;
                id = itemCreated.aggregateId;
                name = itemCreated.name;
                price = itemCreated.price;
                break;
            case ITEM_ADDED:
                stock = ((ItemAdded) event).stock;
                break;
            case ITEM_REMOVED:
                stock = ((ItemReserved) event).newStock;
                break;
            case ITEM_PRICE_CHANGED:
                price = ((ItemPriceChanged) event).newPrice;
                break;
            case ITEM_NAME_CHANGED:
                ItemEvent.ItemNameChanged itemNameChanged = (ItemEvent.ItemNameChanged) event;
                name = itemNameChanged.newName;
                break;
            default:
                throw new RuntimeException(format("Unexpected event type %s for event %s", event.type, event));
        }

        events = events.add(event);
    }

    public void changeName(String name) {
        checkName(name);
        applyEvent(new ItemEvent.ItemNameChanged(id, name));
    }

    public void changePrice(double newPrice) {
        checkPrice(newPrice);
        applyEvent(new ItemPriceChanged(id, newPrice));
    }

    public void add(int number) {
        checkNumberIsPositive(number);
        applyEvent(new ItemAdded(id, stock + number));
    }

    public void remove(int number) {
        checkNumberIsPositive(number);
        checkNumberIsPositive(stock - number);
        applyEvent(new ItemAdded(id, stock - number));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item that = (Item) o;
        return this.stock == that.stock &&
               Double.compare(this.price, that.price) == 0 &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.id, that.id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("name", name)
                          .add("stock", stock)
                          .add("price", price)
                          .toString();
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

}
