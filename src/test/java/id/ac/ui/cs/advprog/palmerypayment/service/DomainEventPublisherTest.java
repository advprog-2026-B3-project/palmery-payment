package id.ac.ui.cs.advprog.palmerypayment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.palmerypayment.event.DomainEventMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainEventPublisherTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ListOperations<String, String> listOperations = mock(ListOperations.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final DomainEventPublisher publisher =
            new DomainEventPublisher(redisTemplate, objectMapper, "palmery.domain.events");

    @Test
    void publishSerializesInstantAndPushesEventToQueue() throws Exception {
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        DomainEventMessage event = publisher.publish(
                "HASIL_PANEN_APPROVED",
                Map.of("hasilPanenId", "panen-1", "userId", "buruh-1", "quantityKg", 100)
        );

        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
        verify(listOperations).rightPush(eq("palmery.domain.events"), eventCaptor.capture());

        DomainEventMessage serialized = objectMapper.readValue(eventCaptor.getValue(), DomainEventMessage.class);
        assertEquals(event.getEventId(), serialized.getEventId());
        assertEquals("HASIL_PANEN_APPROVED", serialized.getEventType());
        assertEquals("panen-1", serialized.getPayload().get("hasilPanenId"));
        assertNotNull(serialized.getOccurredAt());
    }

    @Test
    void publishRejectsBlankEventType() {
        assertThrows(IllegalArgumentException.class, () -> publisher.publish(" ", Map.of()));
    }
}
