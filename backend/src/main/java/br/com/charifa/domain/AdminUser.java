package br.com.charifa.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_users")
public class AdminUser {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String username;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AdminUser() {}
    public AdminUser(String username, String passwordHash) {
        this.id = UUID.randomUUID(); this.username = username; this.passwordHash = passwordHash;
        this.enabled = true; this.createdAt = Instant.now();
    }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEnabled() { return enabled; }
    public void changePassword(String passwordHash) { this.passwordHash = passwordHash; }
}
