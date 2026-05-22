package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.MidtransNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.security.CurrentUser;
import id.ac.ui.cs.advprog.palmerypayment.service.TopUpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentGatewayController {

    private final TopUpService topUpService;

    public PaymentGatewayController(TopUpService topUpService) {
        this.topUpService = topUpService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTopUp(
            @RequestBody CreateTopUpRequest request,
            JwtAuthenticationToken authentication
    ) {
        try {
            CurrentUser currentUser = CurrentUser.from(authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(topUpService.create(request, currentUser.userId()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody MidtransNotificationRequest request) {
        try {
            return ResponseEntity.ok(topUpService.handleMidtransNotification(request));
        } catch (IllegalArgumentException exception) {
            HttpStatus status = "invalid Midtrans signature".equals(exception.getMessage())
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/{reference}")
    public ResponseEntity<?> getTopUpStatus(
            @PathVariable String reference,
            JwtAuthenticationToken authentication,
            @RequestParam(defaultValue = "false") boolean refresh
    ) {
        try {
            CurrentUser currentUser = CurrentUser.from(authentication);
            return ResponseEntity.ok(refresh
                    ? topUpService.syncStatus(reference, currentUser.userId(), currentUser.isAdmin())
                    : topUpService.getByReference(reference, currentUser.userId(), currentUser.isAdmin()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
