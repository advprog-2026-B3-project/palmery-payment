package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;
import java.util.List;

public record WalletDashboardView(
        String userId,
        BigDecimal balance,
        List<PayrollHistoryItem> payrollHistory
) {
}
