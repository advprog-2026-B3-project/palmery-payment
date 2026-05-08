package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.event.DomainEventMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    public DomainEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.broker.exchange}") String exchangeName
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    public DomainEventMessage publish(String eventType, Map<String, Object> payload) {
        String normalizedEventType = normalizeEventType(eventType);
        DomainEventMessage event = new DomainEventMessage(
                UUID.randomUUID().toString(),
                normalizedEventType,
                Instant.now(),
                payload == null ? Map.of() : payload
        );
        rabbitTemplate.convertAndSend(exchangeName, toRoutingKey(normalizedEventType), event);
        return event;
    }

    private String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }
        return eventType.trim();
    }

    private String toRoutingKey(String eventType) {
        return "domain." + eventType
                .trim()
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("[^A-Za-z0-9]+", "-")
                .toLowerCase(Locale.ROOT);
    }
}
