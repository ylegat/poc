package com.zenika.poc.item;

import com.zenika.poc.share.Event;

import java.util.Objects;

import static com.zenika.poc.item.ItemEvent.StockEventType.*;

public class ItemEvent extends Event {

    public enum StockEventType {
        ITEM_CREATED,
        ITEM_ADDED,
        ITEM_REMOVED,
        ITEM_PRICE_CHANGED,
        ITEM_NAME_CHANGED
    }

    public final StockEventType type;

    public ItemEvent(String itemId, StockEventType type) {
        super(itemId);
        this.type = type;
    }

    public boolean isType(StockEventType stockEventType) {
        return type == stockEventType;
    }

    @Override
    public String eventType() {
        return type.name();
    }

    public static class ItemCreated extends ItemEvent {

        public final String name;

        public final int stock;

        public final double price;

        public ItemCreated(String itemId, String name, int stock, double price) {
            super(itemId, ITEM_CREATED);
            this.name = name;
            this.stock = stock;
            this.price = price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemCreated)) return false;
            if (!super.equals(o)) return false;
            ItemCreated that = (ItemCreated) o;
            return stock == that.stock &&
                   Double.compare(that.price, price) == 0 &&
                   Objects.equals(name, that.name);
        }
    }

    public static class ItemNameChanged extends ItemEvent {

        public final String newName;

        public ItemNameChanged(String itemId, String newName) {
            super(itemId, ITEM_NAME_CHANGED);
            this.newName = newName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemNameChanged)) return false;
            if (!super.equals(o)) return false;
            ItemNameChanged that = (ItemNameChanged) o;
            return Objects.equals(newName, that.newName);
        }
    }

    public static class ItemAdded extends ItemEvent {

        public final int stock;

        public ItemAdded(String itemId, int stock) {
            super(itemId, ITEM_ADDED);
            this.stock = stock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemAdded)) return false;
            if (!super.equals(o)) return false;
            ItemAdded itemAdded = (ItemAdded) o;
            return stock == itemAdded.stock;
        }
    }

    public static class ItemReserved extends ItemEvent {

        public final int newStock;

        public ItemReserved(String itemId, int newStock) {
            super(itemId, ITEM_REMOVED);
            this.newStock = newStock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemReserved)) return false;
            if (!super.equals(o)) return false;
            ItemReserved that = (ItemReserved) o;
            return newStock == that.newStock;
        }
    }

    public static class ItemPriceChanged extends ItemEvent {

        public final double newPrice;

        public ItemPriceChanged(String itemId, double newPrice) {
            super(itemId, ITEM_PRICE_CHANGED);
            this.newPrice = newPrice;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemPriceChanged)) return false;
            if (!super.equals(o)) return false;
            ItemPriceChanged that = (ItemPriceChanged) o;
            return Double.compare(that.newPrice, newPrice) == 0;
        }
    }
}
