package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.util.List;

public record NotificationInboxView(
        String userId,
        long unreadCount,
        List<NotificationView> notifications
) {
}
