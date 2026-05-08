package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollHistoryItem;
import id.ac.ui.cs.advprog.palmerypayment.dto.WalletDashboardView;
import id.ac.ui.cs.advprog.palmerypayment.service.WalletDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void getWalletReturnsPayloadFromService() {
        WalletDashboardView wallet = new WalletDashboardView(
                "user-10",
                new BigDecimal("2500.00"),
                List.of(new PayrollHistoryItem(
                        10L,
                        "BURUH",
                        "APPROVED",
                        new BigDecimal("1000.00"),
                        "Payroll Maret",
                        new BigDecimal("100.00"),
                        new BigDecimal("12.00"),
                        "90% x 100.00 Kg x SawitDollar 12.00/Kg",
                        null,
                        Instant.parse("2026-03-01T10:00:00Z"),
                        Instant.parse("2026-03-01T11:00:00Z")
                ))
        );
        when(walletDashboardService.getWalletDashboard("user-10", null, null, null)).thenReturn(wallet);

        ResponseEntity<WalletDashboardView> response = walletController.getWallet("user-10", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(wallet, response.getBody());
        verify(walletDashboardService).getWalletDashboard("user-10", null, null, null);
    }
}
