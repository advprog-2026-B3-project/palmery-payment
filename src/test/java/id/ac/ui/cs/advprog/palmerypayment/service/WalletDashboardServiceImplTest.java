package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.model.Payroll;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PayrollRepository;
import id.ac.ui.cs.advprog.palmerypayment.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletDashboardServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private WalletDashboardServiceImpl walletDashboardService;

    @Test
    void getWalletDashboardReturnsZeroBalanceWhenWalletMissing() {
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        WalletDashboardView result = walletDashboardService.getWalletDashboard("user-1");

        assertEquals("user-1", result.userId());
        assertEquals(BigDecimal.ZERO, result.balance());
        assertTrue(result.payrollHistory().isEmpty());
        verify(walletRepository).findByUserId("user-1");
        verifyNoInteractions(payrollRepository);
    }

    @Test
    void getWalletDashboardReturnsBalanceAndPayrollHistory() {
        Wallet wallet = new Wallet("user-2", new BigDecimal("1250.50"));
        when(walletRepository.findByUserId("user-2")).thenReturn(Optional.of(wallet));

        Payroll payroll = new Payroll(wallet, new BigDecimal("500.00"), "Payroll Februari");
        payroll.prePersist();
        ReflectionTestUtils.setField(payroll, "paidAt", Instant.parse("2026-02-01T08:00:00Z"));
        when(payrollRepository.findByWalletOrderByPaidAtDesc(wallet)).thenReturn(List.of(payroll));

        WalletDashboardView result = walletDashboardService.getWalletDashboard("user-2");

        assertEquals("user-2", result.userId());
        assertEquals(new BigDecimal("1250.50"), result.balance());
        assertEquals(1, result.payrollHistory().size());
        assertEquals(new BigDecimal("500.00"), result.payrollHistory().getFirst().amount());
        assertEquals("Payroll Februari", result.payrollHistory().getFirst().description());
        assertEquals(Instant.parse("2026-02-01T08:00:00Z"), result.payrollHistory().getFirst().paidAt());

        verify(walletRepository).findByUserId("user-2");
        verify(payrollRepository).findByWalletOrderByPaidAtDesc(wallet);
    }

    @Test
    void getWalletDashboardRejectsBlankUserId() {
        assertThrows(IllegalArgumentException.class, () -> walletDashboardService.getWalletDashboard(" "));
    }
}
