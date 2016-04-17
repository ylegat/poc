package com.zenika.poc.bill;

import com.zenika.poc.bill.exception.BillClosedException;
import com.zenika.poc.bill.exception.UnexpectedPaymentException;
import com.zenika.poc.bill.exception.UnpaidBillException;
import com.zenika.poc.share.Events;
import org.assertj.core.api.StrictAssertions;
import org.junit.Test;

import static com.zenika.poc.bill.Bill.createBill;
import static com.zenika.poc.bill.Bill.loadBill;
import static com.zenika.poc.bill.Order.emptyOrder;
import static com.zenika.poc.bill.Order.order;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

public class BillTest {

    public static final Item COFFEE = new Item("coffee", 1.5);

    @Test
    public void should_create_bill() {
        // Given When
        Bill bill = createBill();

        // Then
        StrictAssertions.assertThat(bill.id).isNotNull();
        assertThat(bill.isClosed()).isFalse();
        assertThat(bill).isEqualTo(loadBill(bill.events()));
    }

    @Test
    public void should_order() {
        // Given
        Bill bill = createBill();

        // When
        bill = bill.order(order(COFFEE, 1));

        // Then
        assertThat(bill).isEqualTo(createBill(order(COFFEE, 1), emptyOrder(), false));
        assertThat(bill).isEqualTo(loadBill(bill.events()));
    }

    @Test
    public void should_pay_order() {
        // Given
        Bill bill = createBill();
        bill = bill.order(order(COFFEE, 1));

        // When
        bill = bill.pay(order(COFFEE, 1));

        // Then
        assertThat(bill).isEqualTo(createBill(order(COFFEE, 1), order(COFFEE, 1), false));
        assertThat(bill).isEqualTo(loadBill(bill.events()));
    }

    @Test
    public void should_fail_when_unexpected_payment() {
        // Given
        Bill bill = createBill();

        // When
        Throwable throwable = catchThrowable(() -> bill.pay(order(COFFEE, 1)));

        // Then
        assertThat(throwable).isInstanceOf(UnexpectedPaymentException.class);
        assertThat(bill).isEqualTo(loadBill(doubleEvents(bill.events())));
    }

    @Test
    public void should_close_bill() {
        // Given
        Bill bill = createBill();

        // When
        bill = bill.close();

        // Then
        assertThat(bill).isEqualTo(createBill(emptyOrder(), emptyOrder(), true));
        assertThat(bill).isEqualTo(loadBill(doubleEvents(bill.events())));
    }

    @Test
    public void should_fail_to_order_when_bill_is_closed() {
        // Given
        Bill bill = createBill().close();

        // When
        Throwable throwable = catchThrowable(() -> bill.order(order(COFFEE, 1)));

        // Then
        assertThat(throwable).isInstanceOf(BillClosedException.class);
        assertThat(bill).isEqualTo(loadBill(doubleEvents(bill.events())));
    }

    @Test
    public void should_fail_to_pay_when_bill_is_closed() {
        // Given
        Bill bill = createBill().close();

        // When
        Throwable throwable = catchThrowable(() -> bill.pay(order(COFFEE, 1)));

        // Then
        assertThat(throwable).isInstanceOf(BillClosedException.class);
        assertThat(bill).isEqualTo(loadBill(doubleEvents(bill.events())));
    }

    @Test
    public void should_fail_to_close_when_expecting_payment() {
        // Given
        Bill bill = createBill().order(order(COFFEE, 1));

        // When
        Throwable throwable = catchThrowable(bill::close);

        // Then
        assertThat(throwable).isInstanceOf(UnpaidBillException.class);
        assertThat(bill).isEqualTo(loadBill(doubleEvents(bill.events())));
    }

    private Events<BillEvent> doubleEvents(Events<BillEvent> events) {
        return new Events<>(events.stream()
                                  .flatMap(event -> asList(event, event).stream())
                                  .collect(toList()));
    }
}