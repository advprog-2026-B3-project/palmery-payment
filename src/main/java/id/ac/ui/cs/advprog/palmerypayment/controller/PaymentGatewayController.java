package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpWebhookRequest;
import id.ac.ui.cs.advprog.palmerypayment.service.TopUpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/payments")
public class PaymentGatewayController {

    private final TopUpService topUpService;

    @Value("${payment.gateway.webhook-secret:dev-topup-secret}")
    private String webhookSecret;

    public PaymentGatewayController(TopUpService topUpService) {
        this.topUpService = topUpService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTopUp(@RequestBody CreateTopUpRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(topUpService.create(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String providedSecret,
            @RequestBody TopUpWebhookRequest request
    ) {
        if (!webhookSecret.equals(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid webhook secret"));
        }
        if (!"paid".equalsIgnoreCase(request.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "unsupported webhook status"));
        }

        try {
            return ResponseEntity.ok(topUpService.confirm(request.getReference()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
