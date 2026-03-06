package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PayrollHistoryItem(
        BigDecimal amount,
        String description,
        Instant paidAt
) {
}
