package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeleteEntityType entityType;

    @Column(nullable = false, length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditActionType actionType;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String oldDataJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String newDataJson;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "performed_by_role", length = 50)
    private Role performedByRole;

    @Column(length = 1000)
    private String remarks;

    @Column(length = 100)
    private String ipAddress;

    @Column(nullable = false)
    private Instant performedAt;

    @PrePersist
    public void onCreate() {
        if (this.performedAt == null) {
            this.performedAt = Instant.now();
        }
    }
}