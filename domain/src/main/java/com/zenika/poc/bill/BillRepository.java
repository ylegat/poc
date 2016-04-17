package com.zenika.poc.bill;

import com.zenika.poc.share.Events;

public interface BillRepository {

    Bill findById(String id);

    void save(Events<? extends BillEvent> events);

}
