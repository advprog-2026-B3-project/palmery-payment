package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationResult;
import id.ac.ui.cs.advprog.palmerypayment.dto.NotificationInboxView;
import id.ac.ui.cs.advprog.palmerypayment.dto.NotificationView;
import id.ac.ui.cs.advprog.palmerypayment.model.AppUser;
import id.ac.ui.cs.advprog.palmerypayment.model.Notification;
import id.ac.ui.cs.advprog.palmerypayment.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserDirectoryService userDirectoryService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getMyNotificationsReturnsUnreadCountAndItems() {
        AppUser user = new AppUser("user-1", "BURUH", "User One");
        Notification notification = new Notification(user, "Halo", "Ada tugas baru", "PenugasanBaru");
        notification.prePersist();

        when(notificationRepository.findByTargetUser_UserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(notification));
        when(notificationRepository.countByTargetUser_UserIdAndStatus("user-1", "UNREAD"))
                .thenReturn(1L);

        NotificationInboxView result = notificationService.getMyNotifications("user-1");

        assertEquals(1L, result.unreadCount());
        assertEquals(1, result.notifications().size());
    }

    @Test
    void markAsReadRejectsWhenNotificationDoesNotBelongToUser() {
        when(notificationRepository.findByIdAndTargetUser_UserId(1L, "user-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(1L, "user-1"));
    }

    @Test
    void broadcastCreatesOneNotificationPerResolvedTarget() {
        AppUser user = new AppUser("user-2", "MANDOR", "User Two");
        BroadcastNotificationRequest request = new BroadcastNotificationRequest();
        request.setTitle("Broadcast");
        request.setDescription("Demo");
        request.setUserIds(List.of("user-2"));

        when(userDirectoryService.resolveTargets(List.of("user-2"), null, null)).thenReturn(List.of(user));
        when(notificationRepository.save(ArgumentMatchers.any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BroadcastNotificationResult result = notificationService.broadcast(request);

        assertEquals(1, result.createdCount());
        assertEquals(List.of("user-2"), result.targetUserIds());
    }

    @Test
    void createForUserEnsuresTargetExistsAndPersistsNotification() {
        AppUser user = new AppUser("user-3", "SUPIR", "User Three");
        Notification stored = new Notification(user, "Judul", "Isi", "Manual");
        stored.prePersist();

        when(userDirectoryService.ensureUser("user-3")).thenReturn(user);
        when(notificationRepository.save(ArgumentMatchers.any(Notification.class))).thenReturn(stored);

        NotificationView result = notificationService.createForUser("user-3", "Judul", "Isi", "Manual");

        assertEquals("user-3", result.userId());
        assertEquals("Judul", result.title());
    }
}
