package com.zenika.poc.item;

import com.zenika.poc.bill.Bill;
import com.zenika.poc.share.Events;

public interface ItemRepository {

    Bill findById(String id);

    void save(Events<? extends ItemEvent> events);

}
