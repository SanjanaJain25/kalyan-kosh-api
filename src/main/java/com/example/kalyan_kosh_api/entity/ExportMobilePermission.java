package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "export_mobile_permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_export_mobile_permission_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_export_mobile_permission_user", columnList = "user_id"),
                @Index(name = "idx_export_mobile_permission_enabled", columnList = "enabled")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ExportMobilePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User/manager who is allowed or denied mobile export access.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean enabled = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "granted_by_id")
    private User grantedBy;

    private Instant grantedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;

    private Instant revokedAt;

    @Column(length = 1000)
    private String remarks;
}