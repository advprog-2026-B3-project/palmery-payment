package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.MidtransNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpView;
import id.ac.ui.cs.advprog.palmerypayment.service.TopUpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayControllerTest {

    @Mock
    private TopUpService topUpService;

    @InjectMocks
    private PaymentGatewayController paymentGatewayController;

    @Test
    void createTopUpReturnsCreatedPayload() {
        CreateTopUpRequest request = new CreateTopUpRequest();
        request.setAmountRupiah(new BigDecimal("10000000.00"));
        request.setPaymentMethod("qris");

        TopUpView topUpView = new TopUpView(
                "TOPUP-1234",
                "admin-utama",
                new BigDecimal("10000000.00"),
                new BigDecimal("1000.00"),
                "PENDING",
                "https://app.sandbox.midtrans.com/snap/v2/vtweb/test",
                Instant.parse("2026-04-01T00:00:00Z"),
                null
        );
        when(topUpService.create(request, "admin-utama")).thenReturn(topUpView);

        ResponseEntity<?> response = paymentGatewayController.createTopUp(request, authentication("admin-utama", "ADMIN"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(topUpView, response.getBody());
    }

    @Test
    void webhookRejectsInvalidSignature() {
        when(topUpService.handleMidtransNotification(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalArgumentException("invalid Midtrans signature"));

        MidtransNotificationRequest request = new MidtransNotificationRequest();
        request.setOrderId("TOPUP-1234");
        request.setTransactionStatus("settlement");

        ResponseEntity<?> response = paymentGatewayController.handleWebhook(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private JwtAuthenticationToken authentication(String subject, String role) {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", subject, "role", role)
        );
        return new JwtAuthenticationToken(jwt, List.of());
    }
}
