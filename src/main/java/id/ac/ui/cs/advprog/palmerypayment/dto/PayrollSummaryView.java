package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PayrollSummaryView(
        Long id,
        String userId,
        String type,
        String status,
        BigDecimal amount,
        BigDecimal quantityKg,
        BigDecimal ratePerKg,
        String description,
        String calculationDetail,
        String rejectionReason,
        Instant createdAt,
        Instant processedAt
) {
}
