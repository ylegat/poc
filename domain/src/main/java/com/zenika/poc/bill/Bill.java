package com.zenika.poc.bill;

import com.zenika.poc.bill.BillEvent.BillOpened;
import com.zenika.poc.bill.BillEvent.OrderPaid;
import com.zenika.poc.bill.BillEvent.OrderTaken;
import com.zenika.poc.bill.exception.BillClosedException;
import com.zenika.poc.bill.exception.UnexpectedPaymentException;
import com.zenika.poc.bill.exception.UnpaidBillException;
import com.zenika.poc.share.Aggregate;
import com.zenika.poc.share.Events;

import java.util.Objects;

import static com.zenika.poc.share.Events.emptyEvents;
import static com.zenika.poc.share.Events.singletonEvents;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class Bill extends Aggregate<Bill, BillEvent> {

    private static final Bill INIT_BILL = new Bill(null, null, null, null);

    public static Bill createBill() {
        String billId = randomUUID().toString();
        return new Bill(billId, singletonEvents(new BillOpened(billId)), Order.emptyOrder(), Order.emptyOrder());
    }

    protected static Bill createBill(Order itemsOrdered, Order itemsPaid, boolean closed) {
        String billId = randomUUID().toString();
        return new Bill(billId, emptyEvents(), itemsOrdered, itemsPaid, closed);
    }

    public static Bill loadBill(Events<BillEvent> events) {
        return INIT_BILL.applyEvents(events);
    }

    private final Order itemsOrdered;

    private final Order itemsPaid;

    private final boolean closed;

    private Bill(String id, Events<BillEvent> events, Order itemsOrdered, Order itemsPaid) {
        this(id, events, itemsOrdered, itemsPaid, false);
    }

    private Bill(String id, Events<BillEvent> events, Order itemsOrdered, Order itemsPaid, boolean closed) {
        super(id, events);
        this.itemsOrdered = itemsOrdered;
        this.itemsPaid = itemsPaid;
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isOpen() {
        return !closed;
    }

    public boolean isPaid() {
        return itemsOrdered.equals(itemsPaid);
    }

    public boolean isNotPaid() {
        return !isPaid();
    }

    @Override
    protected Bill applyEvent(BillEvent event) {
        switch (event.type) {
            case BILL_OPENED:
                return new Bill(event.aggregateId, singletonEvents(event), Order.emptyOrder(), Order.emptyOrder());
            case ORDER_TAKEN:
                OrderTaken orderTaken = (OrderTaken) event;
                return new Bill(event.aggregateId, events.add(orderTaken), orderTaken.orderedItem, itemsPaid);
            case BILL_PAID:
                OrderPaid orderPaid = (OrderPaid) event;
                return new Bill(event.aggregateId, events.add(orderPaid), itemsOrdered, orderPaid.itemPaid);
            case BILL_CLOSED:
                return new Bill(event.aggregateId, events.add(event), itemsOrdered, itemsPaid, true);
            default:
                throw new RuntimeException(format("Unexpected event type %s for event %s", event.type, event));
        }
    }

    public Bill order(Order itemsOrdered) {
        checkIsOpen();
        OrderTaken orderTaken = new OrderTaken(id, this.itemsOrdered.add(itemsOrdered));
        return applyEvent(orderTaken);
    }

    public Bill pay(Order itemsPaid) {
        checkIsOpen();
        checkIsPaymentExpected(itemsPaid);
        OrderPaid orderPaid = new OrderPaid(id, this.itemsPaid.add(itemsPaid));
        return applyEvent(orderPaid);
    }

    public Bill close() {
        checkIsOpen();
        checkIsPaid();
        BillEvent.BillClosed billClosed = new BillEvent.BillClosed(id);
        return applyEvent(billClosed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill)) return false;
        Bill that = (Bill) o;
        return closed == that.closed &&
               Objects.equals(itemsOrdered, that.itemsOrdered) &&
               Objects.equals(itemsPaid, that.itemsPaid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Bill{" +
                "itemsOrdered=" + itemsOrdered +
                ", itemsPaid=" + itemsPaid +
                ", closed=" + closed +
                '}';
    }

    private void checkIsPaid() {
        if (isNotPaid()) {
            throw new UnpaidBillException();
        }
    }

    private void checkIsOpen() {
        if (isClosed()) {
            throw new BillClosedException();
        }
    }

    private void checkIsPaymentExpected(Order order) {
        Order leftToPay = itemsOrdered.remove(this.itemsPaid);
        if (!leftToPay.contains(order)) {
            throw new UnexpectedPaymentException();
        }
    }
}
