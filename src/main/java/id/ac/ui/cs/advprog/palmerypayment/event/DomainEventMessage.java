package id.ac.ui.cs.advprog.palmerypayment.event;

import java.time.Instant;
import java.util.Map;

public class DomainEventMessage {

    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private Map<String, Object> payload;

    public DomainEventMessage() {
        // Jackson constructor
    }

    public DomainEventMessage(String eventId, String eventType, Instant occurredAt, Map<String, Object> payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
