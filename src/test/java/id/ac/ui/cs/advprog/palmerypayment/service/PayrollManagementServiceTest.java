package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.GeneratePayrollRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollSummaryView;
import id.ac.ui.cs.advprog.palmerypayment.model.Payroll;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PayrollRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollManagementServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private WageConfigService wageConfigService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private PayrollManagementService payrollManagementService;

    @Test
    void generateDraftCreatesPendingPayrollFromRateAndQuantity() {
        Wallet wallet = new Wallet("buruh-1", BigDecimal.ZERO);
        GeneratePayrollRequest request = new GeneratePayrollRequest();
        request.setUserId("buruh-1");
        request.setRole("buruh");
        request.setQuantityKg(new BigDecimal("100.00"));
        request.setDescription("Panen Approved");

        when(walletService.getOrCreateWallet("buruh-1")).thenReturn(wallet);
        when(wageConfigService.getRateForRole("BURUH")).thenReturn(new BigDecimal("12.00"));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollSummaryView result = payrollManagementService.generateDraft(request);

        assertEquals("PENDING", result.status());
        assertEquals("BURUH", result.type());
        assertEquals(new BigDecimal("1080.00"), result.amount());
        assertEquals("Panen Approved", result.description());
    }

    @Test
    void approveTransfersBalanceAndMarksPayrollAccepted() {
        Wallet recipientWallet = new Wallet("supir-1", BigDecimal.ZERO);
        Wallet adminWallet = new Wallet("admin-1", new BigDecimal("5000.00"));
        Payroll payroll = new Payroll(
                recipientWallet,
                new BigDecimal("250.00"),
                "Payroll Supir",
                "SUPIR",
                "PENDING",
                new BigDecimal("25.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                "detail"
        );

        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
        when(walletService.getOrCreateWallet("admin-1")).thenReturn(adminWallet);
        when(payrollRepository.save(payroll)).thenReturn(payroll);

        PayrollSummaryView result = payrollManagementService.approve(1L, "admin-1");

        assertEquals("ACCEPTED", result.status());
        verify(walletService).subtractBalance(adminWallet, new BigDecimal("250.00"));
        verify(walletService).addBalance(recipientWallet, new BigDecimal("250.00"));
        verify(domainEventPublisher).publish(org.mockito.ArgumentMatchers.eq("PAYROLL_APPROVED"), any());
    }

    @Test
    void rejectRequiresReason() {
        assertThrows(IllegalArgumentException.class, () -> payrollManagementService.reject(10L, " "));
    }

    @Test
    void generateFromEventReturnsExistingPayrollWhenSourceEventAlreadyProcessed() {
        Wallet wallet = new Wallet("mandor-1", BigDecimal.ZERO);
        Payroll payroll = new Payroll(
                wallet,
                new BigDecimal("150.00"),
                "Existing payroll",
                "MANDOR",
                "PENDING",
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                new BigDecimal("10.00"),
                "detail"
        );

        when(payrollRepository.findBySourceEventId("evt-duplicate")).thenReturn(Optional.of(payroll));

        PayrollSummaryView result = payrollManagementService.generateFromEvent(
                "evt-duplicate",
                "PengirimanApprovedAdmin",
                "mandor-1",
                "MANDOR",
                new BigDecimal("10.00"),
                "ignored",
                "ignored"
        );

        assertEquals("MANDOR", result.type());
        verify(payrollRepository).findBySourceEventId("evt-duplicate");
    }

    @Test
    void generateFromEventReturnsExistingPayrollWhenBusinessSourceAlreadyProcessedForRole() {
        Wallet wallet = new Wallet("supir-1", BigDecimal.ZERO);
        Payroll payroll = new Payroll(
                wallet,
                new BigDecimal("270.00"),
                "Existing supir payroll",
                "SUPIR",
                "PENDING",
                new BigDecimal("30.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                "detail"
        );

        when(payrollRepository.findBySourceTypeAndSourceIdAndType(
                "PENGIRIMAN",
                "pengiriman-1",
                "SUPIR"
        )).thenReturn(Optional.of(payroll));

        PayrollSummaryView result = payrollManagementService.generateFromEvent(
                "evt-new",
                "PengirimanApprovedMandor",
                "PENGIRIMAN",
                "pengiriman-1",
                "supir-1",
                "SUPIR",
                new BigDecimal("30.00"),
                "ignored",
                "ignored"
        );

        assertEquals("SUPIR", result.type());
        verify(payrollRepository).findBySourceTypeAndSourceIdAndType("PENGIRIMAN", "pengiriman-1", "SUPIR");
    }

    @Test
    void generateFromEventAttachesBusinessSourceForNewPayroll() {
        Wallet wallet = new Wallet("buruh-1", BigDecimal.ZERO);

        when(walletService.getOrCreateWallet("buruh-1")).thenReturn(wallet);
        when(wageConfigService.getRateForRole("BURUH")).thenReturn(new BigDecimal("12.00"));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollSummaryView result = payrollManagementService.generateFromEvent(
                "evt-harvest",
                "PanenApproved",
                "HASIL_PANEN",
                "harvest-1",
                "buruh-1",
                "BURUH",
                new BigDecimal("100.00"),
                "Panen disetujui",
                "fallback"
        );

        assertEquals("HASIL_PANEN", result.sourceType());
        assertEquals("harvest-1", result.sourceId());
        assertEquals(new BigDecimal("1080.00"), result.amount());
    }
}
