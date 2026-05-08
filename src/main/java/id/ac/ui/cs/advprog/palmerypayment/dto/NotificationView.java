package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.time.Instant;

public record NotificationView(
        Long id,
        String userId,
        String title,
        String description,
        String eventType,
        String status,
        Instant createdAt,
        Instant readAt
) {
}
