package com.zenika.poc.bill;

import com.zenika.poc.bill.exception.NegativeOrderException;
import org.junit.Test;

import static com.zenika.poc.bill.Order.emptyOrder;
import static com.zenika.poc.bill.Order.order;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

public class OrderTest {

    public static final Item COFFEE = new Item("coffee", 1.5);
    public static final Item CAKE = new Item("cake", 3);
    public static final Item CROISSANT = new Item("croissant", 1.10);

    @Test
    public void should_consider_equals_empty_order_and_order_with_explicit_0() {
        assertThat(order(COFFEE, 0)).isEqualTo(emptyOrder());
    }

    @Test
    public void should_fail_when_order_is_negative() {
        // Given When
        Throwable throwable = catchThrowable(() -> order(COFFEE, -1));

        // Then
        assertThat(throwable).isInstanceOf(NegativeOrderException.class);
    }

    @Test
    public void should_add_single_item() {
        // Given
        Order order = order(COFFEE, 2);

        // When
        order = order.add(COFFEE, 2);

        // Then
        assertThat(order).isEqualTo(order(COFFEE, 4));
    }

    @Test
    public void should_add_multiple_items() {
        // Given
        Order order = order(COFFEE, 2, CROISSANT, 1);

        // When
        order = order.add(order(COFFEE, 2, CAKE, 1));

        // Then
        assertThat(order).isEqualTo(order(COFFEE, 4, CAKE, 1, CROISSANT, 1));
    }

    @Test
    public void should_remove_single_item() {
        // Given
        Order order = order(COFFEE, 2);

        // When
        Order result = order.remove(COFFEE, 2);

        // Then
        assertThat(result).isEqualTo(emptyOrder());
    }

    @Test
    public void should_remove_multiple_items() {
        // Given
        Order order = order(COFFEE, 2, CROISSANT, 1, CAKE, 3);

        // When
        Order result = order.remove(order(COFFEE, 1, CROISSANT, 1));

        // Then
        assertThat(result).isEqualTo(order(COFFEE, 1, CAKE, 3));
    }

    @Test
    public void should_contains() {
        // Given
        Order order1 = order(COFFEE, 2, CAKE, 1);
        Order order2 = order(COFFEE, 2);

        // When
        boolean contains = order1.contains(order2);

        // Then
        assertThat(contains).isTrue();
    }

    @Test
    public void should_not_contains_1() {
        // Given
        Order order1 = order(COFFEE, 2, CAKE, 1);
        Order order2 = order(CAKE, 2);

        // When
        boolean contains = order1.contains(order2);

        // Then
        assertThat(contains).isFalse();
    }

    @Test
    public void should_not_contains_2() {
        // Given
        Order order1 = order(COFFEE, 2, CAKE, 1);
        Order order2 = order(COFFEE, 3);

        // When
        boolean contains = order1.contains(order2);

        // Then
        assertThat(contains).isFalse();
    }

}