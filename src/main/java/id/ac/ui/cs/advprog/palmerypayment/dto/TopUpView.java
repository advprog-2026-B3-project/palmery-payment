package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TopUpView(
        String reference,
        String adminUserId,
        BigDecimal amountRupiah,
        BigDecimal creditedAmount,
        String status,
        String gatewayUrl,
        Instant createdAt,
        Instant paidAt
) {
}
