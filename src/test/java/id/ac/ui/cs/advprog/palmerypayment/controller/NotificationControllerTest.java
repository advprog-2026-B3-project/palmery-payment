package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationResult;
import id.ac.ui.cs.advprog.palmerypayment.dto.NotificationInboxView;
import id.ac.ui.cs.advprog.palmerypayment.dto.NotificationView;
import id.ac.ui.cs.advprog.palmerypayment.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void broadcastReturnsCreatedResult() {
        BroadcastNotificationRequest request = new BroadcastNotificationRequest();
        request.setTitle("Info");
        request.setDescription("Desc");
        request.setUserIds(List.of("user-1"));

        when(notificationService.broadcast(request)).thenReturn(
                new BroadcastNotificationResult(1, List.of("user-1"))
        );

        ResponseEntity<?> response = notificationController.broadcast(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void myNotificationsReturnsInboxForAuthenticatedUser() {
        NotificationInboxView inbox = new NotificationInboxView(
                "user-1",
                1,
                List.of(new NotificationView(
                        1L,
                        "user-1",
                        "Notif",
                        "Desc",
                        "BROADCAST",
                        "UNREAD",
                        Instant.parse("2026-05-01T00:00:00Z"),
                        null
                ))
        );
        when(notificationService.getMyNotifications("user-1")).thenReturn(inbox);

        ResponseEntity<?> response = notificationController.myNotifications(authentication("user-1", "BURUH"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(inbox, response.getBody());
    }

    @Test
    void markAsReadReturnsBadRequestForUnknownUser() {
        when(notificationService.markAsRead(1L, "user-1"))
                .thenThrow(new IllegalArgumentException("notification not found"));

        ResponseEntity<?> response = notificationController.markAsRead(1L, authentication("user-1", "BURUH"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("notification not found", ((Map<?, ?>) response.getBody()).get("message"));
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
