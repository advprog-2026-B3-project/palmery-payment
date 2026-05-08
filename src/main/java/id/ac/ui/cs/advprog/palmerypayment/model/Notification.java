package id.ac.ui.cs.advprog.palmerypayment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser targetUser;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant readAt;

    protected Notification() {
        // JPA constructor
    }

    public Notification(AppUser targetUser, String title, String description, String eventType) {
        this.targetUser = targetUser;
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.status = "UNREAD";
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getTargetUser() {
        return targetUser;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void markAsRead() {
        this.status = "READ";
        if (readAt == null) {
            this.readAt = Instant.now();
        }
    }
}
