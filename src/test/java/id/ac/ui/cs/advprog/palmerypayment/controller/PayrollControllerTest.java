package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.GeneratePayrollRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollSummaryView;
import id.ac.ui.cs.advprog.palmerypayment.service.PayrollManagementService;
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
class PayrollControllerTest {

    @Mock
    private PayrollManagementService payrollManagementService;

    @InjectMocks
    private PayrollController payrollController;

    @Test
    void generatePayrollReturnsCreatedPayload() {
        GeneratePayrollRequest request = new GeneratePayrollRequest();
        request.setUserId("buruh-1");
        request.setRole("BURUH");
        request.setQuantityKg(new BigDecimal("100.00"));

        PayrollSummaryView summary = new PayrollSummaryView(
                1L,
                "buruh-1",
                "BURUH",
                "PENDING",
                new BigDecimal("1080.00"),
                new BigDecimal("100.00"),
                new BigDecimal("12.00"),
                "Payroll Buruh",
                "detail",
                null,
                Instant.parse("2026-04-01T00:00:00Z"),
                null
        );
        when(payrollManagementService.generateDraft(request)).thenReturn(summary);

        ResponseEntity<?> response = payrollController.generatePayroll(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(summary, response.getBody());
    }

    @Test
    void approvePayrollUsesAuthenticatedAdminSubject() {
        PayrollSummaryView summary = new PayrollSummaryView(
                1L,
                "buruh-1",
                "BURUH",
                "APPROVED",
                new BigDecimal("1080.00"),
                new BigDecimal("100.00"),
                new BigDecimal("12.00"),
                "Payroll Buruh",
                "detail",
                null,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-01T00:05:00Z")
        );
        when(payrollManagementService.approve(1L, "admin-utama")).thenReturn(summary);

        ResponseEntity<?> response = payrollController.approvePayroll(1L, null, authentication("admin-utama", "ADMIN"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
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
