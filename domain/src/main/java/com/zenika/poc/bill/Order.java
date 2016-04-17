package com.zenika.poc.bill;

import com.google.common.collect.ImmutableMap;
import com.zenika.poc.bill.exception.NegativeOrderException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;

public class Order {

    public static Order emptyOrder() {
        return new Order();
    }

    public static Order order(Item i1, int n1) {
        return new Order(i1, n1);
    }

    public static Order order(Item i1, int n1, Item i2, int n2) {
        return new Order(ImmutableMap.of(i1, n1, i2, n2));
    }

    public static Order order(Item i1, int n1, Item i2, int n2, Item i3, int n3) {
        return new Order(ImmutableMap.of(i1, n1, i2, n2, i3, n3));
    }

    private final Map<Item, Integer> items;

    public Order() {
        this(emptyMap());
    }

    public Order(Item i1, int n1) {
        this(singletonMap(i1, n1));
    }

    public Order(Map<Item, Integer> items) {
        this.items = items.entrySet()
                          .stream()
                          .peek(e -> checkPositiveOrder(e.getValue()))
                          .filter(e -> e.getValue() > 0)
                          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Order add(Item thatItem, int thatNumber) {
        Map<Item, Integer> thisItems = new HashMap<>(items);
        add(thisItems, thatItem, thatNumber);
        return new Order(thisItems);
    }

    public Order add(Order that) {
        Map<Item, Integer> thisItems = new HashMap<>(items);
        that.items.forEach((thatItem, thanNumber) -> add(thisItems, thatItem, thanNumber));
        return new Order(thisItems);
    }

    public Order remove(Item thatItem, int thatNumber) {
        return add(thatItem, -thatNumber);
    }

    public Order remove(Order that) {
        Map<Item, Integer> thisItems = new HashMap<>(items);
        that.items.forEach((thatItem, thatNumber) -> add(thisItems, thatItem, -thatNumber));
        return new Order(thisItems);
    }

    public boolean contains(Order that) {
        try {
            remove(that);
            return true;
        } catch (NegativeOrderException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return "Order{" +
                "items=" + items +
                '}';
    }

    private Integer add(Map<Item, Integer> thisItems, Item thatItem, Integer thanNumber) {
        return thisItems.compute(thatItem, (ignore, thisNumber) -> (thisNumber == null) ? thanNumber : thisNumber + thanNumber);
    }

    private void checkPositiveOrder(int value) {
        if (value < 0) throw new NegativeOrderException();
    }

}
