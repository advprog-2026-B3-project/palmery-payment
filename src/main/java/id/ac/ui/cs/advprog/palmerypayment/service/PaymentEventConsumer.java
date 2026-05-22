package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.event.DomainEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PaymentEventConsumer {

    private final UserDirectoryService userDirectoryService;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final PayrollManagementService payrollManagementService;

    public PaymentEventConsumer(
            UserDirectoryService userDirectoryService,
            WalletService walletService,
            NotificationService notificationService,
            PayrollManagementService payrollManagementService
    ) {
        this.userDirectoryService = userDirectoryService;
        this.walletService = walletService;
        this.notificationService = notificationService;
        this.payrollManagementService = payrollManagementService;
    }

    public void consume(DomainEventMessage event) {
        if (event == null || event.getEventType() == null || event.getEventType().isBlank()) {
            log.warn("Skipping empty broker event");
            return;
        }

        String eventType = event.getEventType().trim();
        Map<String, Object> payload = event.getPayload() == null ? Map.of() : event.getPayload();

        try {
            switch (eventType.toUpperCase(Locale.ROOT)) {
                case "USERBARU" -> handleUserCreated(payload);
                case "PENUGASANBARU" -> handleAssignmentEvent(payload, eventType);
                case "PANENAPPROVED", "HASIL_PANEN_APPROVED" -> handleHarvestApproved(event.getEventId(), payload);
                case "PENGIRIMANAPPROVEDMANDOR", "PENGIRIMAN_APPROVED_BY_MANDOR" -> handleShipmentApprovedByMandor(event.getEventId(), payload);
                case "PENGIRIMANAPPROVEDADMIN", "PENGIRIMAN_APPROVED_BY_ADMIN", "PENGIRIMAN_PARTIALLY_APPROVED_BY_ADMIN" -> handleShipmentApprovedByAdmin(event.getEventId(), payload);
                case "PANENREJECTED", "PENGIRIMANTIBA", "PAYROLLDIPROSES", "PAYROLL_APPROVED", "PAYROLL_REJECTED" -> handleNotificationOnlyEvent(payload, eventType);
                default -> log.info("Ignoring unsupported event type {}", eventType);
            }
        } catch (RuntimeException exception) {
            log.error("Failed to process event {}: {}", eventType, exception.getMessage(), exception);
            throw exception;
        }
    }

    private void handleUserCreated(Map<String, Object> payload) {
        String userId = firstString(payload, "userId", "targetUserId");
        if (userId == null) {
            log.warn("UserBaru missing userId payload");
            return;
        }
        String role = firstString(payload, "role", "roleKey", "userRole");
        String displayName = firstString(payload, "displayName", "fullName", "name");
        userDirectoryService.upsertUser(userId, role, displayName);
        walletService.initializeWallet(userId);
    }

    private void handleAssignmentEvent(Map<String, Object> payload, String eventType) {
        List<String> targetUserIds = collectTargetUserIds(payload);
        if (targetUserIds.isEmpty()) {
            log.warn("{} missing target user payload", eventType);
            return;
        }

        String title = firstString(payload, "title");
        if (title == null) {
            title = "Penugasan baru diterima";
        }
        String description = firstString(payload, "description", "message");
        if (description == null) {
            String assignmentType = firstString(payload, "assignmentType", "type");
            String sourceName = firstString(payload, "sourceName", "assignedBy", "plantationName", "mandorName");
            description = buildAssignmentDescription(assignmentType, sourceName);
        }

        for (String targetUserId : targetUserIds) {
            notificationService.createForUser(targetUserId, title, description, eventType);
        }
    }

    private void handleHarvestApproved(String eventId, Map<String, Object> payload) {
        String userId = firstString(payload, "userId", "buruhUserId", "workerUserId", "workerId", "targetUserId");
        BigDecimal quantityKg = firstDecimal(payload, "quantityKg", "kg", "approvedKg", "kgHarvested");
        String sourceId = firstString(payload, "sourceId", "harvestId", "hasilPanenId");
        if (userId == null || quantityKg == null) {
            log.warn("PanenApproved missing userId or quantityKg");
            return;
        }

        payrollManagementService.generateFromEvent(
                eventId,
                "PanenApproved",
                "HASIL_PANEN",
                sourceId,
                userId,
                "BURUH",
                quantityKg,
                firstString(payload, "description", "title"),
                "Panen approved untuk " + quantityKg + " Kg"
        );
        notificationService.createForUser(
                userId,
                firstString(payload, "title") == null ? "Panen disetujui" : firstString(payload, "title"),
                firstString(payload, "message", "description") == null
                        ? "Payroll buruh otomatis dibuat dari panen yang disetujui."
                        : firstString(payload, "message", "description"),
                "PanenApproved"
        );
    }

    private void handleShipmentApprovedByMandor(String eventId, Map<String, Object> payload) {
        String userId = firstString(payload, "userId", "supirUserId", "driverUserId", "supirId", "targetUserId");
        BigDecimal quantityKg = firstDecimal(payload, "quantityKg", "kg", "approvedKg", "totalKg");
        String sourceId = firstString(payload, "sourceId", "pengirimanId");
        if (userId == null || quantityKg == null) {
            log.warn("PengirimanApprovedMandor missing userId or quantityKg");
            return;
        }

        payrollManagementService.generateFromEvent(
                eventId,
                "PengirimanApprovedMandor",
                "PENGIRIMAN",
                sourceId,
                userId,
                "SUPIR",
                quantityKg,
                firstString(payload, "description", "title"),
                "Pengiriman disetujui mandor untuk " + quantityKg + " Kg"
        );
        notificationService.createForUser(
                userId,
                firstString(payload, "title") == null ? "Pengiriman disetujui mandor" : firstString(payload, "title"),
                firstString(payload, "message", "description") == null
                        ? "Payroll supir otomatis dibuat setelah pengiriman disetujui mandor."
                        : firstString(payload, "message", "description"),
                "PengirimanApprovedMandor"
        );
    }

    private void handleShipmentApprovedByAdmin(String eventId, Map<String, Object> payload) {
        String userId = firstString(payload, "userId", "mandorUserId", "mandorId", "targetUserId");
        BigDecimal quantityKg = firstDecimal(payload, "kgDiakui", "recognizedKg", "approvedKg", "quantityKg", "kg");
        String sourceId = firstString(payload, "sourceId", "pengirimanId");
        if (userId == null || quantityKg == null) {
            log.warn("PengirimanApprovedAdmin missing userId or kgDiakui");
            return;
        }

        payrollManagementService.generateFromEvent(
                eventId,
                "PengirimanApprovedAdmin",
                "PENGIRIMAN",
                sourceId,
                userId,
                "MANDOR",
                quantityKg,
                firstString(payload, "description", "title"),
                "Pengiriman diakui admin untuk " + quantityKg + " Kg"
        );
        notificationService.createForUser(
                userId,
                firstString(payload, "title") == null ? "Pengiriman disetujui admin" : firstString(payload, "title"),
                firstString(payload, "message", "description") == null
                        ? "Payroll mandor otomatis dibuat dari pengiriman yang diakui admin."
                        : firstString(payload, "message", "description"),
                "PengirimanApprovedAdmin"
        );
    }

    private void handleNotificationOnlyEvent(Map<String, Object> payload, String eventType) {
        List<String> targetUserIds = collectTargetUserIds(payload);
        if (targetUserIds.isEmpty()) {
            log.warn("{} missing target user payload", eventType);
            return;
        }

        String title = firstString(payload, "title");
        if (title == null) {
            title = defaultTitle(eventType);
        }
        String description = firstString(payload, "message", "description");
        if (description == null) {
            description = defaultDescription(eventType, payload);
        }

        for (String targetUserId : targetUserIds) {
            notificationService.createForUser(targetUserId, title, description, eventType);
        }
    }

    private List<String> collectTargetUserIds(Map<String, Object> payload) {
        Set<String> uniqueUserIds = new LinkedHashSet<>();
        addIfPresent(uniqueUserIds, firstString(payload, "targetUserId", "userId"));
        addIfPresent(uniqueUserIds, firstString(payload, "buruhUserId"));
        addIfPresent(uniqueUserIds, firstString(payload, "supirUserId"));
        addIfPresent(uniqueUserIds, firstString(payload, "mandorUserId"));

        Object rawList = payload.get("targetUserIds");
        if (rawList instanceof List<?> entries) {
            for (Object entry : entries) {
                if (entry != null && !entry.toString().isBlank()) {
                    uniqueUserIds.add(entry.toString().trim());
                }
            }
        }

        return new ArrayList<>(uniqueUserIds);
    }

    private void addIfPresent(Set<String> values, String candidate) {
        if (candidate != null && !candidate.isBlank()) {
            values.add(candidate.trim());
        }
    }

    private String firstString(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString().trim();
            }
        }
        return null;
    }

    private BigDecimal firstDecimal(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Number number) {
                return BigDecimal.valueOf(number.doubleValue()).setScale(2, RoundingMode.HALF_UP);
            }
            try {
                return new BigDecimal(value.toString().trim()).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                // try next key
            }
        }
        return null;
    }

    private String buildAssignmentDescription(String assignmentType, String sourceName) {
        if (assignmentType == null && sourceName == null) {
            return "Ada penugasan baru yang perlu ditindaklanjuti.";
        }
        if (sourceName == null) {
            return "Penugasan baru diterima untuk " + assignmentType + ".";
        }
        if (assignmentType == null) {
            return "Penugasan baru diterima dari " + sourceName + ".";
        }
        return "Penugasan " + assignmentType + " baru diterima dari " + sourceName + ".";
    }

    private String defaultTitle(String eventType) {
        return switch (eventType.toUpperCase(Locale.ROOT)) {
            case "PANENREJECTED" -> "Panen ditolak";
            case "PENGIRIMANTIBA" -> "Pengiriman tiba";
            case "PAYROLLDIPROSES" -> "Payroll diproses";
            default -> "Notifikasi Palmery";
        };
    }

    private String defaultDescription(String eventType, Map<String, Object> payload) {
        return (switch (eventType.toUpperCase(Locale.ROOT)) {
            case "PANENREJECTED" ->
                    "Panen Anda ditolak. " + safeSuffix(firstString(payload, "reason", "alasan"));
            case "PENGIRIMANTIBA" ->
                    "Pengiriman telah tiba dan menunggu tindak lanjut.";
            case "PAYROLLDIPROSES" ->
                    "Status payroll terbaru: " + firstString(payload, "status");
            default -> "Ada pembaruan baru pada akun Anda.";
        }).trim();
    }

    private String safeSuffix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return "Alasan: " + value.trim();
    }
}
