package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "delete_requests")
@Getter
@Setter
@NoArgsConstructor
public class DeleteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeleteEntityType entityType;

    @Column(nullable = false, length = 100)
    private String entityId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_by_role", nullable = false, length = 50)
    private Role requestedByRole;

    @Column(name = "requested_from_dashboard", length = 50)
    private String requestedFromDashboard;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeleteRequestStatus status = DeleteRequestStatus.PENDING;

    @Column(name = "approval_level", length = 50)
    private String approvalLevel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rejected_by")
    private User rejectedBy;

    private Instant rejectedAt;

    @Column(length = 500)
    private String rejectionReason;

    private Instant executedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restore_requested_by")
    private User restoreRequestedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restore_approved_by")
    private User restoreApprovedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = DeleteRequestStatus.PENDING;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}