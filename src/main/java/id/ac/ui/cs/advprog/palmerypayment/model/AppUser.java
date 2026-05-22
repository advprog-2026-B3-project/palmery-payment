package id.ac.ui.cs.advprog.palmerypayment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @Column(nullable = false, unique = true, length = 100)
    private String userId;

    @Column(nullable = false, length = 50)
    private String roleKey;

    @Column(length = 150)
    private String displayName;

    @Column(nullable = false)
    private Instant createdAt;

    protected AppUser() {
        // JPA constructor
    }

    public AppUser(String userId, String roleKey, String displayName) {
        this.userId = userId;
        this.roleKey = roleKey;
        this.displayName = displayName;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
