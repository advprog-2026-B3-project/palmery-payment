package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpView;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpWebhookRequest;
import id.ac.ui.cs.advprog.palmerypayment.service.TopUpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;

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
        request.setAdminUserId("admin-utama");
        request.setAmountRupiah(new BigDecimal("10000000.00"));

        TopUpView topUpView = new TopUpView(
                "TOPUP-1234",
                "admin-utama",
                new BigDecimal("10000000.00"),
                new BigDecimal("1000.00"),
                "PENDING",
                "https://sandbox.palmery.local/pay/TOPUP-1234",
                Instant.parse("2026-04-01T00:00:00Z"),
                null
        );
        when(topUpService.create(request)).thenReturn(topUpView);

        ResponseEntity<?> response = paymentGatewayController.createTopUp(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(topUpView, response.getBody());
    }

    @Test
    void webhookRejectsInvalidSecret() {
        ReflectionTestUtils.setField(paymentGatewayController, "webhookSecret", "expected-secret");

        TopUpWebhookRequest request = new TopUpWebhookRequest();
        request.setReference("TOPUP-1234");
        request.setStatus("paid");

        ResponseEntity<?> response = paymentGatewayController.handleWebhook("wrong-secret", request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
