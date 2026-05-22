package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.GeneratePayrollRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.PayrollSummaryView;
import id.ac.ui.cs.advprog.palmerypayment.model.Payroll;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PayrollRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PayrollManagementService {

    private static final BigDecimal DEDUCTION_MULTIPLIER = new BigDecimal("0.90");
    private static final BigDecimal DEDUCTION_RATE = new BigDecimal("10.00");
    private static final List<String> SUPPORTED_ROLES = List.of("BURUH", "SUPIR", "MANDOR");

    private final PayrollRepository payrollRepository;
    private final WalletService walletService;
    private final WageConfigService wageConfigService;
    private final DomainEventPublisher domainEventPublisher;

    public PayrollManagementService(
            PayrollRepository payrollRepository,
            WalletService walletService,
            WageConfigService wageConfigService,
            DomainEventPublisher domainEventPublisher
    ) {
        this.payrollRepository = payrollRepository;
        this.walletService = walletService;
        this.wageConfigService = wageConfigService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional(readOnly = true)
    public List<PayrollSummaryView> listPayrolls(String status, String userId) {
        String normalizedStatus = normalizeOptionalStatus(status);
        String normalizedUserId = normalizeOptionalUserId(userId);

        return payrollRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(payroll -> normalizedStatus == null || payroll.getStatus().equals(normalizedStatus))
                .filter(payroll -> normalizedUserId == null || payroll.getWallet().getUserId().equalsIgnoreCase(normalizedUserId))
                .map(this::toSummaryView)
                .toList();
    }

    @Transactional
    public PayrollSummaryView generateDraft(GeneratePayrollRequest request) {
        String description = request.getDescription();
        if (description == null || description.isBlank()) {
            description = "Payroll otomatis " + normalizeRole(request.getRole()).toLowerCase(Locale.ROOT);
        }

        return createPayroll(
                request.getUserId(),
                request.getRole(),
                request.getQuantityKg(),
                description,
                null,
                null,
                null,
                null
        );
    }

    @Transactional
    public PayrollSummaryView approve(Long payrollId, String adminUserId) {
        Payroll payroll = findPendingPayroll(payrollId);
        Wallet adminWallet = walletService.getOrCreateWallet(adminUserId);
        walletService.subtractBalance(adminWallet, payroll.getAmount());
        walletService.addBalance(payroll.getWallet(), payroll.getAmount());
        payroll.approve();
        Payroll savedPayroll = payrollRepository.save(payroll);
        publishPayrollProcessedEvent(savedPayroll);
        return toSummaryView(savedPayroll);
    }

    @Transactional
    public PayrollSummaryView reject(Long payrollId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }

        Payroll payroll = findPendingPayroll(payrollId);
        payroll.reject(reason.trim());
        Payroll savedPayroll = payrollRepository.save(payroll);
        publishPayrollProcessedEvent(savedPayroll);
        return toSummaryView(savedPayroll);
    }

    @Transactional
    public PayrollSummaryView generateFromEvent(
            String sourceEventId,
            String sourceEventType,
            String sourceType,
            String sourceId,
            String userId,
            String role,
            BigDecimal quantityKg,
            String description,
            String fallbackDescription
    ) {
        String normalizedRole = normalizeRole(role);
        String normalizedSourceType = normalizeOptionalSourceType(sourceType);
        String normalizedSourceId = normalizeOptionalSourceId(sourceId);

        if (normalizedSourceType != null && normalizedSourceId != null) {
            Payroll existingPayroll = payrollRepository
                    .findBySourceTypeAndSourceIdAndType(normalizedSourceType, normalizedSourceId, normalizedRole)
                    .orElse(null);
            if (existingPayroll != null) {
                return toSummaryView(existingPayroll);
            }
        }

        if (sourceEventId != null && !sourceEventId.isBlank()) {
            Payroll existingPayroll = payrollRepository.findBySourceEventId(sourceEventId.trim()).orElse(null);
            if (existingPayroll != null) {
                return toSummaryView(existingPayroll);
            }
        }

        String resolvedDescription = description;
        if (resolvedDescription == null || resolvedDescription.isBlank()) {
            resolvedDescription = fallbackDescription;
        }
        if (resolvedDescription == null || resolvedDescription.isBlank()) {
            resolvedDescription = "Payroll otomatis dari event " + normalizedRole.toLowerCase(Locale.ROOT);
        }

        return createPayroll(
                userId,
                normalizedRole,
                quantityKg,
                resolvedDescription,
                sourceEventId,
                sourceEventType,
                normalizedSourceType,
                normalizedSourceId
        );
    }

    @Transactional
    public PayrollSummaryView generateFromEvent(
            String sourceEventId,
            String sourceEventType,
            String userId,
            String role,
            BigDecimal quantityKg,
            String description,
            String fallbackDescription
    ) {
        return generateFromEvent(
                sourceEventId,
                sourceEventType,
                null,
                null,
                userId,
                role,
                quantityKg,
                description,
                fallbackDescription
        );
    }

    private PayrollSummaryView createPayroll(
            String userId,
            String role,
            BigDecimal quantityKg,
            String description,
            String sourceEventId,
            String sourceEventType,
            String sourceType,
            String sourceId
    ) {
        if (quantityKg == null || quantityKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantityKg must be positive");
        }

        String normalizedRole = normalizeRole(role);
        Wallet wallet = walletService.getOrCreateWallet(userId);
        BigDecimal normalizedQuantityKg = quantityKg.setScale(2, RoundingMode.HALF_UP);
        BigDecimal ratePerKg = wageConfigService.getRateForRole(normalizedRole).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = normalizedQuantityKg
                .multiply(ratePerKg)
                .multiply(DEDUCTION_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);
        String detail = "90% x " + normalizedQuantityKg + " Kg x SawitDollar " + ratePerKg + "/Kg";

        Payroll payroll = new Payroll(
                wallet,
                amount,
                description,
                normalizedRole,
                "PENDING",
                normalizedQuantityKg,
                ratePerKg,
                DEDUCTION_RATE,
                detail
        );
        if (sourceEventId != null && !sourceEventId.isBlank()) {
            payroll.attachEventSource(sourceEventId.trim(), sourceEventType);
        }
        if (sourceType != null && sourceId != null) {
            payroll.attachBusinessSource(sourceType, sourceId);
        }

        return toSummaryView(payrollRepository.save(payroll));
    }

    private Payroll findPendingPayroll(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("payroll not found"));
        if (!"PENDING".equals(payroll.getStatus())) {
            throw new IllegalStateException("payroll has already been processed");
        }
        return payroll;
    }

    private PayrollSummaryView toSummaryView(Payroll payroll) {
        return new PayrollSummaryView(
                payroll.getId(),
                payroll.getWallet().getUserId(),
                payroll.getType(),
                payroll.getStatus(),
                payroll.getAmount(),
                payroll.getQuantityKg(),
                payroll.getRatePerKg(),
                payroll.getDescription(),
                payroll.getCalculationDetail(),
                payroll.getSourceType(),
                payroll.getSourceId(),
                payroll.getRejectionReason(),
                payroll.getCreatedAt(),
                payroll.getProcessedAt()
        );
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role is required");
        }
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("unsupported role: " + role);
        }
        return normalizedRole;
    }

    private String normalizeOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return userId.trim();
    }

    private String normalizeOptionalSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return null;
        }
        return sourceType.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalSourceId(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return sourceId.trim();
    }

    private void publishPayrollProcessedEvent(Payroll payroll) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("payrollId", payroll.getId());
        payload.put("userId", payroll.getWallet().getUserId());
        payload.put("status", payroll.getStatus());
        payload.put("amount", payroll.getAmount());
        payload.put("type", payroll.getType());
        payload.put("sourceType", payroll.getSourceType());
        payload.put("sourceId", payroll.getSourceId());
        payload.put("description", payroll.getDescription());
        payload.put("reason", payroll.getRejectionReason());
        payload.put(
                "title",
                "ACCEPTED".equals(payroll.getStatus()) ? "Payroll disetujui" : "Payroll ditolak"
        );
        payload.put(
                "message",
                "ACCEPTED".equals(payroll.getStatus())
                        ? "Payroll Anda sudah disetujui dan saldo wallet telah diperbarui."
                        : "Payroll Anda ditolak oleh admin."
        );
        domainEventPublisher.publish(
                "ACCEPTED".equals(payroll.getStatus()) ? "PAYROLL_APPROVED" : "PAYROLL_REJECTED",
                payload
        );
    }
}
