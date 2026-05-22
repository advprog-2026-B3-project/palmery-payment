package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.GeneratePayrollRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollDecisionRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollSummaryView;
import id.ac.ui.cs.advprog.palmerypayment.security.CurrentUser;
import id.ac.ui.cs.advprog.palmerypayment.service.PayrollManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    private final PayrollManagementService payrollManagementService;

    public PayrollController(PayrollManagementService payrollManagementService) {
        this.payrollManagementService = payrollManagementService;
    }

    @GetMapping
    public ResponseEntity<List<PayrollSummaryView>> listPayrolls(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            JwtAuthenticationToken authentication
    ) {
        CurrentUser currentUser = CurrentUser.from(authentication);
        String effectiveUserId = currentUser.isAdmin() ? userId : currentUser.userId();
        return ResponseEntity.ok(payrollManagementService.listPayrolls(status, effectiveUserId));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePayroll(@RequestBody GeneratePayrollRequest request) {
        return handle(
                () -> ResponseEntity.status(HttpStatus.CREATED).body(payrollManagementService.generateDraft(request))
        );
    }

    @PatchMapping("/{payrollId}/approve")
    public ResponseEntity<?> approvePayroll(
            @PathVariable Long payrollId,
            @RequestBody(required = false) PayrollDecisionRequest request,
            JwtAuthenticationToken authentication
    ) {
        return handle(() -> {
            CurrentUser currentUser = CurrentUser.from(authentication);
            return ResponseEntity.ok(payrollManagementService.approve(payrollId, currentUser.userId()));
        });
    }

    @PatchMapping("/{payrollId}/reject")
    public ResponseEntity<?> rejectPayroll(
            @PathVariable Long payrollId,
            @RequestBody PayrollDecisionRequest request
    ) {
        return handle(() -> ResponseEntity.ok(payrollManagementService.reject(payrollId, request.getReason())));
    }

    private ResponseEntity<?> handle(ResponseSupplier supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.unprocessableEntity().body(Map.of("message", exception.getMessage()));
        }
    }

    @FunctionalInterface
    private interface ResponseSupplier {
        ResponseEntity<?> get();
    }
}
