package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.config.PaymentGatewayProperties;
import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.MidtransNotificationRequest;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopUpServiceTest {

    @Mock
    private PaymentTopUpRepository paymentTopUpRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private MidtransGatewayClient midtransGatewayClient;

    @Mock
    private PaymentGatewayProperties paymentGatewayProperties;

    @InjectMocks
    private TopUpService topUpService;

    @Test
    void createBuildsPendingTopUpWithConvertedAmount() {
        CreateTopUpRequest request = new CreateTopUpRequest();
        request.setAmountRupiah(new BigDecimal("10000000"));
        request.setPaymentMethod("qris");

        when(paymentGatewayProperties.getFinishUrl()).thenReturn("http://localhost:3000/wallet");
        when(midtransGatewayClient.createSnapTransaction(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.argThat(amount -> amount.compareTo(new BigDecimal("10000000.00")) == 0),
                org.mockito.ArgumentMatchers.eq("admin-utama"),
                org.mockito.ArgumentMatchers.contains("topupReference"),
                org.mockito.ArgumentMatchers.eq("qris")
        )).thenReturn(new MidtransGatewayClient.MidtransCreateResult(
                "snap-token",
                "https://app.sandbox.midtrans.com/snap/v2/vtweb/token"
        ));

        when(paymentTopUpRepository.save(any(PaymentTopUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TopUpView result = topUpService.create(request, "admin-utama");

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
                "https://app.sandbox.midtrans.com/snap/v2/vtweb/test",
                "snap-token",
                "MIDTRANS"
        );
        Wallet adminWallet = new Wallet("admin-utama", BigDecimal.ZERO);

        when(paymentTopUpRepository.findByReference("TOPUP-12345678")).thenReturn(Optional.of(topUp));
        when(walletService.getOrCreateWallet("admin-utama")).thenReturn(adminWallet);

        TopUpView result = topUpService.confirm("TOPUP-12345678");

        assertEquals("PAID", result.status());
        verify(walletService).addBalance(adminWallet, new BigDecimal("500.00"));
        verify(paymentTopUpRepository).save(topUp);
    }

    @Test
    void validatesMidtransSignature() {
        PaymentGatewayProperties.Midtrans midtrans = new PaymentGatewayProperties.Midtrans();
        midtrans.setServerKey("Mid-server-test");
        when(paymentGatewayProperties.getMidtrans()).thenReturn(midtrans);

        MidtransNotificationRequest request = new MidtransNotificationRequest();
        request.setOrderId("TOPUP-TEST");
        request.setStatusCode("200");
        request.setGrossAmount("100000.00");
        request.setSignatureKey(sha512Hex("TOPUP-TEST200100000.00Mid-server-test"));

        assertTrue(topUpService.isValidMidtransSignature(request));
    }

    private String sha512Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
