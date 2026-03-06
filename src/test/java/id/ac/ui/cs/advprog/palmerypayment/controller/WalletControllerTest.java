package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollHistoryItem;
import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.service.WalletDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletDashboardService walletDashboardService;

    @InjectMocks
    private WalletController walletController;

    @Test
    void walletPageAddsWalletModelAndReturnsViewName() {
        WalletDashboardView wallet = new WalletDashboardView(
                "user-10",
                new BigDecimal("2500.00"),
                List.of(new PayrollHistoryItem(
                        new BigDecimal("1000.00"),
                        "Payroll Maret",
                        Instant.parse("2026-03-01T10:00:00Z")
                ))
        );
        when(walletDashboardService.getWalletDashboard("user-10")).thenReturn(wallet);

        Model model = new ExtendedModelMap();
        String viewName = walletController.walletPage("user-10", model);

        assertEquals("wallet", viewName);
        assertSame(wallet, model.getAttribute("wallet"));
        verify(walletDashboardService).getWalletDashboard("user-10");
    }
}
