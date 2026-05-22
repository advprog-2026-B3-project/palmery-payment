package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.event.DomainEventMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private WalletService walletService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PayrollManagementService payrollManagementService;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    @Test
    void consumeUserCreatedEventUpsertsUserAndWallet() {
        paymentEventConsumer.consume(new DomainEventMessage(
                "evt-1",
                "UserBaru",
                Instant.now(),
                0,
                Map.of("userId", "buruh-1", "role", "BURUH", "displayName", "Buruh Demo")
        ));

        verify(userDirectoryService).upsertUser("buruh-1", "BURUH", "Buruh Demo");
        verify(walletService).initializeWallet("buruh-1");
    }

    @Test
    void consumeHarvestApprovedGeneratesPayrollAndNotification() {
        paymentEventConsumer.consume(new DomainEventMessage(
                "evt-2",
                "PanenApproved",
                Instant.now(),
                0,
                Map.of("buruhUserId", "buruh-2", "kg", "120.5")
        ));

        verify(payrollManagementService).generateFromEvent(
                "evt-2",
                "PanenApproved",
                "buruh-2",
                "BURUH",
                new BigDecimal("120.50"),
                null,
                "Panen approved untuk 120.50 Kg"
        );
        verify(notificationService).createForUser(
                "buruh-2",
                "Panen disetujui",
                "Payroll buruh otomatis dibuat dari panen yang disetujui.",
                "PanenApproved"
        );
    }

    @Test
    void consumeNotificationOnlyEventBroadcastsToTargets() {
        paymentEventConsumer.consume(new DomainEventMessage(
                "evt-3",
                "PenugasanBaru",
                Instant.now(),
                0,
                Map.of(
                        "targetUserIds", List.of("buruh-1", "buruh-2"),
                        "title", "Penugasan baru",
                        "description", "Mandor sudah mengirim tugas baru"
                )
        ));

        verify(notificationService).createForUser("buruh-1", "Penugasan baru", "Mandor sudah mengirim tugas baru", "PenugasanBaru");
        verify(notificationService).createForUser("buruh-2", "Penugasan baru", "Mandor sudah mengirim tugas baru", "PenugasanBaru");
    }
}
