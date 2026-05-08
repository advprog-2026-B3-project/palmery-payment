package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.util.Map;

public class PublishEventRequest {

    private String eventType;
    private Map<String, Object> payload;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
