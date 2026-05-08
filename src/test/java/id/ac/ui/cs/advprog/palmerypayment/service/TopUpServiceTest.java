package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpView;
import id.ac.ui.cs.advprog.palmerypayment.model.PaymentTopUp;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PaymentTopUpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopUpServiceTest {

    @Mock
    private PaymentTopUpRepository paymentTopUpRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TopUpService topUpService;

    @Test
    void createBuildsPendingTopUpWithConvertedAmount() {
        CreateTopUpRequest request = new CreateTopUpRequest();
        request.setAdminUserId("admin-utama");
        request.setAmountRupiah(new BigDecimal("10000000"));

        when(paymentTopUpRepository.save(any(PaymentTopUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TopUpView result = topUpService.create(request);

        assertEquals("admin-utama", result.adminUserId());
        assertEquals("PENDING", result.status());
        assertEquals(new BigDecimal("1000.00"), result.creditedAmount());
    }

    @Test
    void confirmCreditsWalletAndMarksTopUpPaid() {
        PaymentTopUp topUp = new PaymentTopUp(
                "TOPUP-12345678",
                "admin-utama",
                new BigDecimal("5000000.00"),
                new BigDecimal("500.00"),
                "PENDING",
                "https://sandbox.palmery.local/pay/TOPUP-12345678"
        );
        Wallet adminWallet = new Wallet("admin-utama", BigDecimal.ZERO);

        when(paymentTopUpRepository.findByReference("TOPUP-12345678")).thenReturn(Optional.of(topUp));
        when(walletService.getOrCreateWallet("admin-utama")).thenReturn(adminWallet);

        TopUpView result = topUpService.confirm("TOPUP-12345678");

        assertEquals("PAID", result.status());
        verify(walletService).addBalance(adminWallet, new BigDecimal("500.00"));
        verify(paymentTopUpRepository).save(topUp);
    }
}
