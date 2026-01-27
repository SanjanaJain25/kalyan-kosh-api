package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Manager Assignment Entity
 * Handles assignment of managers to specific geographical areas (Sambhag/District/Block)
 * Supports many-to-many relationship where one manager can manage multiple areas
 */
@Entity
@Table(name = "manager_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManagerAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;  // The manager user
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManagerLevel managerLevel;  // SAMBHAG, DISTRICT, BLOCK
    
    // Location assignments (nullable based on level)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sambhag_id")
    private Sambhag sambhag;  // For Sambhag managers
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id") 
    private District district;  // For District managers
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;  // For Block managers
    
    @Column(nullable = false)
    private Instant assignedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;  // Admin who made the assignment
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    private String notes;  // Optional notes about the assignment
    
    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}