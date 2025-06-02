package com.ipi.mesi_backend_rpg.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "edit_locks", indexes = {
    @Index(name = "idx_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_user", columnList = "locked_by_id"),
    @Index(name = "idx_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
public class EditLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_id", nullable = false)
    private User lockedBy;
    
    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "lock_token", nullable = false, unique = true)
    private String lockToken;
    
    @Column(name = "lock_scope") // "FULL", "BLOCK", "METADATA"
    private String lockScope;
    
    @Column(name = "metadata", columnDefinition = "TEXT") // JSON pour infos supplémentaires
    private String metadata;
    
    public EditLock(ResourceType resourceType, Long resourceId, User lockedBy, 
                   String lockScope, int durationMinutes) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.lockedBy = lockedBy;
        this.lockScope = lockScope;
        this.lockedAt = LocalDateTime.now();
        this.expiresAt = this.lockedAt.plusMinutes(durationMinutes);
        this.lockToken = UUID.randomUUID().toString();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void extend(int additionalMinutes) {
        this.expiresAt = this.expiresAt.plusMinutes(additionalMinutes);
    }
}