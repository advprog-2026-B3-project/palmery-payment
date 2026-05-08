package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.util.List;

public record BroadcastNotificationResult(
        int createdCount,
        List<String> targetUserIds
) {
}
