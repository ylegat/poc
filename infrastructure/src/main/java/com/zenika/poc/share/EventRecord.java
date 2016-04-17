package com.zenika.poc.share;

public class EventRecord {

    public final String aggregateId;

    public final String eventType;

    public final String clazz;

    public final int version;

    public final String payload;

    public EventRecord(String aggregateId, String eventType, String clazz, int version, String payload) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.clazz = clazz;
        this.version = version;
        this.payload = payload;
    }
}
