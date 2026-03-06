package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollHistoryItem;
import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PayrollRepository;
import id.ac.ui.cs.advprog.palmerypayment.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletDashboardServiceImpl implements WalletDashboardService {

    private final WalletRepository walletRepository;
    private final PayrollRepository payrollRepository;

    public WalletDashboardServiceImpl(
            WalletRepository walletRepository,
            PayrollRepository payrollRepository
    ) {
        this.walletRepository = walletRepository;
        this.payrollRepository = payrollRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletDashboardView getWalletDashboard(String userId) {
        String normalizedUserId = normalizeUserId(userId);

        return walletRepository.findByUserId(normalizedUserId)
                .map(this::toDashboardView)
                .orElseGet(() -> new WalletDashboardView(normalizedUserId, BigDecimal.ZERO, List.of()));
    }

    private WalletDashboardView toDashboardView(Wallet wallet) {
        List<PayrollHistoryItem> history = payrollRepository.findByWalletOrderByPaidAtDesc(wallet).stream()
                .map(payroll -> new PayrollHistoryItem(
                        payroll.getAmount(),
                        payroll.getDescription(),
                        payroll.getPaidAt()
                ))
                .toList();

        return new WalletDashboardView(wallet.getUserId(), wallet.getBalance(), history);
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }
}
