package id.ac.ui.cs.advprog.palmerypayment.controller;

import id.ac.ui.cs.advprog.palmerypayment.dto.BroadcastNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.security.CurrentUser;
import id.ac.ui.cs.advprog.palmerypayment.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notif")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody BroadcastNotificationRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.broadcast(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> myNotifications(JwtAuthenticationToken authentication) {
        try {
            CurrentUser currentUser = CurrentUser.from(authentication);
            return ResponseEntity.ok(notificationService.getMyNotifications(currentUser.userId()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            JwtAuthenticationToken authentication
    ) {
        try {
            CurrentUser currentUser = CurrentUser.from(authentication);
            return ResponseEntity.ok(notificationService.markAsRead(notificationId, currentUser.userId()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
