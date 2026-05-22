package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PayrollHistoryItem(
        Long id,
        String type,
        String status,
        BigDecimal amount,
        String description,
        BigDecimal quantityKg,
        BigDecimal ratePerKg,
        String calculationDetail,
        String rejectionReason,
        Instant createdAt,
        Instant processedAt
) {
}
