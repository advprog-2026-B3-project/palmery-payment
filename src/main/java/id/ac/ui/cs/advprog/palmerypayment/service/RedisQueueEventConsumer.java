package id.ac.ui.cs.advprog.palmerypayment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.palmerypayment.event.DomainEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RedisQueueEventConsumer {

    private static final String DEAD_LETTER_SUFFIX = ".dead-letter";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentEventConsumer paymentEventConsumer;
    private final String queueName;
    private final boolean listenerEnabled;
    private final int batchSize;
    private final int maxAttempts;
    private final Duration pollTimeout;

    public RedisQueueEventConsumer(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            PaymentEventConsumer paymentEventConsumer,
            @Value("${app.event.queue:palmery.domain.events}") String queueName,
            @Value("${app.event.listener.enabled:true}") boolean listenerEnabled,
            @Value("${app.event.listener.batch-size:10}") int batchSize,
            @Value("${app.event.listener.max-attempts:3}") int maxAttempts,
            @Value("${app.event.listener.poll-timeout-ms:250}") long pollTimeoutMs
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.paymentEventConsumer = paymentEventConsumer;
        this.queueName = queueName;
        this.listenerEnabled = listenerEnabled;
        this.batchSize = Math.max(1, batchSize);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.pollTimeout = Duration.ofMillis(Math.max(50L, pollTimeoutMs));
    }

    @Scheduled(fixedDelayString = "${app.event.listener.poll-timeout-ms:250}")
    public void poll() {
        if (!listenerEnabled) {
            return;
        }

        for (int index = 0; index < batchSize; index++) {
            String rawEvent = redisTemplate.opsForList().leftPop(queueName, pollTimeout);
            if (rawEvent == null) {
                return;
            }

            try {
                DomainEventMessage event = objectMapper.readValue(rawEvent, DomainEventMessage.class);
                paymentEventConsumer.consume(event);
            } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
                redisTemplate.opsForList().rightPush(queueName + DEAD_LETTER_SUFFIX, rawEvent);
                log.error("Failed to parse queue event payload; moved to dead letter queue: {}", exception.getMessage(), exception);
            } catch (Exception exception) {
                retryOrDeadLetter(rawEvent, exception);
                return;
            }
        }
    }

    private void retryOrDeadLetter(String rawEvent, Exception exception) {
        try {
            DomainEventMessage event = objectMapper.readValue(rawEvent, DomainEventMessage.class);
            int nextAttempt = event.getAttempts() + 1;
            event.setAttempts(nextAttempt);

            if (nextAttempt >= maxAttempts) {
                redisTemplate.opsForList().rightPush(queueName + DEAD_LETTER_SUFFIX, objectMapper.writeValueAsString(event));
                log.error("Failed to consume queue event after {} attempts; moved to dead letter queue: {}",
                        nextAttempt, exception.getMessage(), exception);
                return;
            }

            redisTemplate.opsForList().rightPush(queueName, objectMapper.writeValueAsString(event));
            log.warn("Failed to consume queue event; scheduled retry {}/{}: {}",
                    nextAttempt, maxAttempts, exception.getMessage(), exception);
        } catch (Exception retryException) {
            redisTemplate.opsForList().rightPush(queueName + DEAD_LETTER_SUFFIX, rawEvent);
            log.error("Failed to requeue queue event; moved original payload to dead letter queue: {}",
                    retryException.getMessage(), retryException);
        }
    }
}
