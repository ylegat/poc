package com.zenika.poc.bill;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Item {

    public final String name;

    public final double price;

    public Item(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Double.compare(item.price, price) == 0 &&
               Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", name)
                          .add("price", price)
                          .toString();
    }
}
