package com.zenika.poc.bill;

import com.zenika.poc.share.Event;

import java.util.Objects;

public abstract class BillEvent extends Event {

    public enum BillEventType {
        BILL_OPENED,
        ORDER_TAKEN,
        BILL_PAID,
        BILL_CLOSED,
    }

    public final BillEventType type;

    public BillEvent(String aggregateId, BillEventType type) {
        super(aggregateId);
        this.type = type;
    }

    @Override
    public String eventType() {
        return type.name();
    }

    public static class BillClosed extends BillEvent {

        public BillClosed(String billId) {
            super(billId, BillEventType.BILL_CLOSED);
        }
    }

    public static class BillOpened extends BillEvent {

        public BillOpened(String billId) {
            super(billId, BillEventType.BILL_OPENED);
        }

    }

    public static class OrderPaid extends BillEvent {

        public final Order itemPaid;

        public OrderPaid(String billId, Order itemPaid) {
            super(billId, BillEventType.BILL_PAID);
            this.itemPaid = itemPaid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrderPaid)) return false;
            if (!super.equals(o)) return false;
            OrderPaid orderPaid = (OrderPaid) o;
            return Objects.equals(itemPaid, orderPaid.itemPaid);
        }

    }

    public static class OrderTaken extends BillEvent {

        public final Order orderedItem;

        public OrderTaken(String billId, Order orderedItem) {
            super(billId, BillEventType.ORDER_TAKEN);
            this.orderedItem = orderedItem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrderTaken)) return false;
            if (!super.equals(o)) return false;
            OrderTaken that = (OrderTaken) o;
            return Objects.equals(orderedItem, that.orderedItem);
        }

    }
}
