package com.example.kalyan_kosh_api.dto.manager;

import com.example.kalyan_kosh_api.entity.QueryPriority;
import com.example.kalyan_kosh_api.entity.QueryStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for Manager Query Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerQueryResponse {
    private Long id;
    private String title;
    private String description;
    
    // Creator details
    private String createdById;
    private String createdByName;
    private String createdByEmail;
    
    // Assignment details
    private String assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    
    // Status and priority
    private QueryPriority priority;
    private QueryStatus status;
    
    // Related location
    private UUID relatedSambhagId;
    private String relatedSambhagName;
    private UUID relatedDistrictId;
    private String relatedDistrictName;
    private UUID relatedBlockId;
    private String relatedBlockName;
    
    // Related user
    private String relatedUserId;
    private String relatedUserName;
    
    // Resolution details
    private String resolution;
    private String resolvedById;
    private String resolvedByName;
    
    // Timestamps
    private Instant createdAt;
    private Instant assignedAt;
    private Instant resolvedAt;
    private Instant updatedAt;
    
    // Computed fields
    private String locationContext;  // Combined location string
    private String statusDisplay;    // User-friendly status
    private String priorityDisplay;  // User-friendly priority
    private long daysSinceCreated;   // Days since query was created
    private boolean isOverdue;       // If query is taking too long
    
    /**
     * Helper method to get location context
     */
    public String getLocationContext() {
        StringBuilder context = new StringBuilder();
        
        if (relatedSambhagName != null) {
            context.append("Sambhag: ").append(relatedSambhagName);
        }
        
        if (relatedDistrictName != null) {
            if (context.length() > 0) context.append(" > ");
            context.append("District: ").append(relatedDistrictName);
        }
        
        if (relatedBlockName != null) {
            if (context.length() > 0) context.append(" > ");
            context.append("Block: ").append(relatedBlockName);
        }
        
        return context.length() > 0 ? context.toString() : "General Query";
    }
    
    /**
     * Helper method for user-friendly status display
     */
    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        
        switch (status) {
            case PENDING: return "Pending Review";
            case IN_PROGRESS: return "In Progress";
            case RESOLVED: return "Resolved";
            case REJECTED: return "Rejected";
            case ESCALATED: return "Escalated";
            default: return status.toString();
        }
    }
    
    /**
     * Helper method for user-friendly priority display
     */
    public String getPriorityDisplay() {
        if (priority == null) return "Medium";
        
        switch (priority) {
            case LOW: return "Low Priority";
            case MEDIUM: return "Medium Priority"; 
            case HIGH: return "High Priority";
            case URGENT: return "Urgent";
            default: return priority.toString();
        }
    }
    
    /**
     * Calculate days since creation
     */
    public long getDaysSinceCreated() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, Instant.now()).toDays();
    }
    
    /**
     * Check if query is overdue (more than 7 days for regular, 3 days for urgent)
     */
    public boolean isOverdue() {
        long days = getDaysSinceCreated();
        if (status == QueryStatus.RESOLVED || status == QueryStatus.REJECTED) {
            return false;
        }
        
        return (priority == QueryPriority.URGENT && days > 3) || 
               (priority != QueryPriority.URGENT && days > 7);
    }
}