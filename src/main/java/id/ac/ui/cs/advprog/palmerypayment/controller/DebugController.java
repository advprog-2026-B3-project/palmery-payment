package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.DebugCheckRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.PublishEventRequest;
import id.ac.ui.cs.advprog.palmerypayment.event.DomainEventMessage;
import id.ac.ui.cs.advprog.palmerypayment.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerypayment.service.DomainEventPublisher;
import id.ac.ui.cs.advprog.palmerypayment.service.IntegrationDebugService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/debug")
public class DebugController {

    private final IntegrationDebugService integrationDebugService;
    private final DomainEventPublisher domainEventPublisher;

    public DebugController(
            IntegrationDebugService integrationDebugService,
            DomainEventPublisher domainEventPublisher
    ) {
        this.integrationDebugService = integrationDebugService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<Map<String, Object>> healthcheck() {
        return ResponseEntity.ok(integrationDebugService.integrationStatus());
    }

    @GetMapping("/integration")
    public ResponseEntity<Map<String, Object>> integration() {
        return ResponseEntity.ok(integrationDebugService.integrationStatus());
    }

    @PostMapping("/checks")
    public ResponseEntity<Map<String, Object>> createCheck(@RequestBody(required = false) DebugCheckRequest request) {
        String source = request == null ? null : request.getSource();
        return ResponseEntity.status(HttpStatus.CREATED).body(integrationDebugService.createCheck(source));
    }

    @GetMapping("/checks")
    public ResponseEntity<List<Map<String, Object>>> latestChecks() {
        List<Map<String, Object>> response = integrationDebugService.latestChecks().stream()
                .map(this::toResponseEntry)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/events")
    public ResponseEntity<?> publishEvent(@RequestBody PublishEventRequest request) {
        try {
            DomainEventMessage publishedEvent = domainEventPublisher.publish(request.getEventType(), request.getPayload());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "eventId", publishedEvent.getEventId(),
                    "eventType", publishedEvent.getEventType(),
                    "occurredAt", publishedEvent.getOccurredAt(),
                    "payload", publishedEvent.getPayload()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    private Map<String, Object> toResponseEntry(IntegrationCheck check) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", check.getId());
        entry.put("source", check.getSource());
        entry.put("created_at", check.getCreatedAt());
        return entry;
    }
}
