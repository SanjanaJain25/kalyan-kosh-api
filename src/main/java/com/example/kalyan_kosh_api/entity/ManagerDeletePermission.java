package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "manager_delete_permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_manager_delete_permission_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_manager_delete_permission_user", columnList = "user_id"),
                @Index(name = "idx_manager_delete_permission_enabled", columnList = "enabled")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ManagerDeletePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Manager user who is allowed/denied delete access
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