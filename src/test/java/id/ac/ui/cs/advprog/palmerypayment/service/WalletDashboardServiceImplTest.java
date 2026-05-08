package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.model.Payroll;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PayrollRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletDashboardServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private WalletDashboardServiceImpl walletDashboardService;

    @Test
    void getWalletDashboardReturnsZeroBalanceWhenWalletHasNoHistory() {
        Wallet wallet = new Wallet("user-1", BigDecimal.ZERO);
        when(walletService.getOrCreateWallet("user-1")).thenReturn(wallet);
        when(payrollRepository.findByWalletOrderByCreatedAtDesc(wallet)).thenReturn(List.of());

        WalletDashboardView result = walletDashboardService.getWalletDashboard("user-1", null, null, null);

        assertEquals("user-1", result.userId());
        assertEquals(BigDecimal.ZERO, result.balance());
        assertTrue(result.payrollHistory().isEmpty());
        verify(walletService).getOrCreateWallet("user-1");
    }

    @Test
    void getWalletDashboardReturnsBalanceAndPayrollHistory() {
        Wallet wallet = new Wallet("user-2", new BigDecimal("1250.50"));
        when(walletService.getOrCreateWallet("user-2")).thenReturn(wallet);

        Payroll payroll = new Payroll(
                wallet,
                new BigDecimal("500.00"),
                "Payroll Februari",
                "BURUH",
                "APPROVED",
                new BigDecimal("50.00"),
                new BigDecimal("12.00"),
                new BigDecimal("10.00"),
                "90% x 50.00 Kg x SawitDollar 12.00/Kg"
        );
        payroll.prePersist();
        ReflectionTestUtils.setField(payroll, "createdAt", Instant.parse("2026-02-01T08:00:00Z"));
        when(payrollRepository.findByWalletOrderByCreatedAtDesc(wallet)).thenReturn(List.of(payroll));

        WalletDashboardView result = walletDashboardService.getWalletDashboard("user-2", null, null, null);

        assertEquals("user-2", result.userId());
        assertEquals(new BigDecimal("1250.50"), result.balance());
        assertEquals(1, result.payrollHistory().size());
        assertEquals(new BigDecimal("500.00"), result.payrollHistory().getFirst().amount());
        assertEquals("Payroll Februari", result.payrollHistory().getFirst().description());
        assertEquals(Instant.parse("2026-02-01T08:00:00Z"), result.payrollHistory().getFirst().createdAt());

        verify(walletService).getOrCreateWallet("user-2");
        verify(payrollRepository).findByWalletOrderByCreatedAtDesc(wallet);
    }

    @Test
    void getWalletDashboardFiltersByStatusAndDate() {
        Wallet wallet = new Wallet("user-3", new BigDecimal("900.00"));
        when(walletService.getOrCreateWallet("user-3")).thenReturn(wallet);

        Payroll pendingPayroll = new Payroll(
                wallet,
                new BigDecimal("100.00"),
                "Pending payroll",
                "BURUH",
                "PENDING",
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                new BigDecimal("10.00"),
                "detail"
        );
        pendingPayroll.prePersist();
        ReflectionTestUtils.setField(pendingPayroll, "createdAt", Instant.parse("2026-02-10T08:00:00Z"));

        Payroll approvedPayroll = new Payroll(
                wallet,
                new BigDecimal("200.00"),
                "Approved payroll",
                "SUPIR",
                "APPROVED",
                new BigDecimal("20.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                "detail"
        );
        approvedPayroll.prePersist();
        ReflectionTestUtils.setField(approvedPayroll, "createdAt", Instant.parse("2026-02-11T08:00:00Z"));

        when(payrollRepository.findByWalletOrderByCreatedAtDesc(wallet)).thenReturn(List.of(pendingPayroll, approvedPayroll));

        WalletDashboardView result = walletDashboardService.getWalletDashboard(
                "user-3",
                "APPROVED",
                LocalDate.parse("2026-02-11"),
                LocalDate.parse("2026-02-11")
        );

        assertEquals(1, result.payrollHistory().size());
        assertEquals("APPROVED", result.payrollHistory().getFirst().status());
    }

    @Test
    void getWalletDashboardRejectsBlankUserId() {
        assertThrows(IllegalArgumentException.class, () ->
                walletDashboardService.getWalletDashboard(" ", null, null, null)
        );
    }
}
