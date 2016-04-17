package com.zenika.poc.bill;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.zenika.poc.bill.BillEvent.BillClosed;
import com.zenika.poc.bill.BillEvent.BillOpened;
import com.zenika.poc.bill.BillEvent.OrderPaid;
import com.zenika.poc.bill.BillEvent.OrderTaken;
import com.zenika.poc.bill.exception.BillClosedException;
import com.zenika.poc.bill.exception.UnexpectedPaymentException;
import com.zenika.poc.bill.exception.UnpaidBillException;
import com.zenika.poc.share.Aggregate;
import com.zenika.poc.share.Events;

import java.util.Objects;

import static com.zenika.poc.bill.Order.emptyOrder;
import static com.zenika.poc.share.Events.emptyEvents;
import static com.zenika.poc.share.Events.singletonEvents;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class Bill extends Aggregate<Bill, BillEvent> {

    @VisibleForTesting
    protected static Bill createBill(String billId, Order itemsOrdered, Order itemsPaid, boolean closed) {
        return new Bill(billId, emptyEvents(), itemsOrdered, itemsPaid, closed);
    }

    public static Bill createBill() {
        String billId = randomUUID().toString();
        return new Bill(billId, singletonEvents(new BillOpened(billId)), emptyOrder(), emptyOrder());
    }

    public static Bill loadBill(Events<BillEvent> events) {
        Bill bill = new Bill(null, emptyEvents(), emptyOrder(), emptyOrder());
        bill.applyEvents(events);
        return bill;
    }

    private Order itemsOrdered;

    private Order itemsPaid;

    private boolean closed;

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
    protected void applyEvent(BillEvent event) {
        switch (event.type) {
            case BILL_OPENED:
                id = event.aggregateId;
                break;
            case ORDER_TAKEN:
                itemsOrdered = ((OrderTaken) event).orderedItem;
                break;
            case BILL_PAID:
                itemsPaid = ((OrderPaid) event).itemPaid;
                break;
            case BILL_CLOSED:
                closed = true;
                break;
            default:
                throw new RuntimeException(format("Unexpected event type %s for event %s", event.type, event));
        }

        events.add(event);
    }

    public void order(Order itemsOrdered) {
        checkIsOpen();
        OrderTaken orderTaken = new OrderTaken(id, this.itemsOrdered.add(itemsOrdered));
        applyEvent(orderTaken);
    }

    public void pay(Order itemsPaid) {
        checkIsOpen();
        checkIsPaymentExpected(itemsPaid);
        OrderPaid orderPaid = new OrderPaid(id, this.itemsPaid.add(itemsPaid));
        applyEvent(orderPaid);
    }

    public void close() {
        checkIsOpen();
        checkIsPaid();
        BillClosed billClosed = new BillClosed(id);
        applyEvent(billClosed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill)) return false;
        Bill bill = (Bill) o;
        return closed == bill.closed &&
               Objects.equals(itemsOrdered, bill.itemsOrdered) &&
               Objects.equals(itemsPaid, bill.itemsPaid) &&
               Objects.equals(id, bill.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemsOrdered, itemsPaid, closed);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("itemsOrdered", itemsOrdered)
                          .add("itemsPaid", itemsPaid)
                          .add("closed", closed)
                          .toString();
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
