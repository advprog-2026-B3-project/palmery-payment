package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollHistoryItem;
import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.model.Payroll;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class WalletDashboardServiceImpl implements WalletDashboardService {

    private final WalletService walletService;
    private final PayrollRepository payrollRepository;

    public WalletDashboardServiceImpl(
            WalletService walletService,
            PayrollRepository payrollRepository
    ) {
        this.walletService = walletService;
        this.payrollRepository = payrollRepository;
    }

    @Override
    @Transactional
    public WalletDashboardView getWalletDashboard(
            String userId,
            String status,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        String normalizedUserId = normalizeUserId(userId);
        Wallet wallet = walletService.getOrCreateWallet(normalizedUserId);
        return toDashboardView(wallet, status, fromDate, toDate);
    }

    private WalletDashboardView toDashboardView(
            Wallet wallet,
            String status,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        List<PayrollHistoryItem> history = payrollRepository.findByWalletOrderByCreatedAtDesc(wallet).stream()
                .filter(payroll -> matchesStatus(payroll, status))
                .filter(payroll -> matchesDateRange(payroll, fromDate, toDate))
                .map(payroll -> new PayrollHistoryItem(
                        payroll.getId(),
                        payroll.getType(),
                        payroll.getStatus(),
                        payroll.getAmount(),
                        payroll.getDescription(),
                        payroll.getQuantityKg(),
                        payroll.getRatePerKg(),
                        payroll.getCalculationDetail(),
                        payroll.getRejectionReason(),
                        payroll.getCreatedAt(),
                        payroll.getProcessedAt()
                ))
                .toList();

        return new WalletDashboardView(wallet.getUserId(), wallet.getBalance(), history);
    }

    private boolean matchesStatus(Payroll payroll, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return payroll.getStatus().equalsIgnoreCase(status.trim());
    }

    private boolean matchesDateRange(Payroll payroll, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && payroll.getCreatedAt().isBefore(fromDate.atStartOfDay().toInstant(ZoneOffset.UTC))) {
            return false;
        }
        if (toDate != null && !payroll.getCreatedAt().isBefore(toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))) {
            return false;
        }
        return true;
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }
}
