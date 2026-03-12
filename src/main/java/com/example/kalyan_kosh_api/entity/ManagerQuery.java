package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Manager Query Entity
 * Handles queries/issues raised by managers that need resolution
 * Supports hierarchical escalation system
 */
@Entity
@Table(name = "manager_queries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManagerQuery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;  // Manager who created query
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to") 
    private User assignedTo;  // Higher level manager or admin
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryPriority priority = QueryPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryStatus status = QueryStatus.PENDING;
    
    // Query can be related to specific location
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_sambhag_id")
    private Sambhag relatedSambhag;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_district_id")  
    private District relatedDistrict;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_block_id")
    private Block relatedBlock;
    
    // Query can be related to specific user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_user_id")
    private User relatedUser;
    
    @Column(columnDefinition = "TEXT")
    private String resolution;  // Resolution/response text
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;  // Who resolved the query
    
    @Column(nullable = false)
    private Instant createdAt;
    
    private Instant assignedAt;
    private Instant resolvedAt;
    private Instant updatedAt;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        
        if (status == QueryStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = Instant.now();
        }
        
        if (assignedTo != null && assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}