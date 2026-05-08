package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationResult;
import id.ac.ui.cs.advprog.palmerypayment.dto.NotificationInboxView;
import id.ac.ui.cs.advprog.palmerypayment.dto.NotificationView;
import id.ac.ui.cs.advprog.palmerypayment.model.AppUser;
import id.ac.ui.cs.advprog.palmerypayment.model.Notification;
import id.ac.ui.cs.advprog.palmerypayment.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserDirectoryService userDirectoryService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserDirectoryService userDirectoryService
    ) {
        this.notificationRepository = notificationRepository;
        this.userDirectoryService = userDirectoryService;
    }

    @Transactional(readOnly = true)
    public NotificationInboxView getMyNotifications(String userId) {
        String normalizedUserId = requireUserId(userId);
        List<NotificationView> notifications = notificationRepository
                .findByTargetUser_UserIdOrderByCreatedAtDesc(normalizedUserId)
                .stream()
                .map(this::toView)
                .toList();
        long unreadCount = notificationRepository.countByTargetUser_UserIdAndStatus(normalizedUserId, "UNREAD");
        return new NotificationInboxView(normalizedUserId, unreadCount, notifications);
    }

    @Transactional
    public NotificationView markAsRead(Long notificationId, String userId) {
        Notification notification = notificationRepository
                .findByIdAndTargetUser_UserId(notificationId, requireUserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("notification not found"));
        notification.markAsRead();
        return toView(notificationRepository.save(notification));
    }

    @Transactional
    public BroadcastNotificationResult broadcast(BroadcastNotificationRequest request) {
        validateMessage(request.getTitle(), request.getDescription());
        List<AppUser> targets = userDirectoryService.resolveTargets(
                request.getUserIds(),
                request.getRoleTarget(),
                request.getAll()
        );
        List<Notification> saved = targets.stream()
                .map(user -> notificationRepository.save(new Notification(
                        user,
                        request.getTitle().trim(),
                        request.getDescription().trim(),
                        "BROADCAST"
                )))
                .toList();
        return new BroadcastNotificationResult(
                saved.size(),
                saved.stream().map(notification -> notification.getTargetUser().getUserId()).toList()
        );
    }

    @Transactional
    public NotificationView createForUser(String userId, String title, String description, String eventType) {
        validateMessage(title, description);
        AppUser targetUser = userDirectoryService.ensureUser(userId);
        Notification notification = notificationRepository.save(new Notification(
                targetUser,
                title.trim(),
                description.trim(),
                normalizeEventType(eventType)
        ));
        return toView(notification);
    }

    private NotificationView toView(Notification notification) {
        return new NotificationView(
                notification.getId(),
                notification.getTargetUser().getUserId(),
                notification.getTitle(),
                notification.getDescription(),
                notification.getEventType(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private void validateMessage(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId.trim();
    }

    private String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return "MANUAL";
        }
        return eventType.trim();
    }
}
