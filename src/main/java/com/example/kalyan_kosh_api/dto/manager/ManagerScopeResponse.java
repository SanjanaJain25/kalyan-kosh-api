package com.example.kalyan_kosh_api.dto.manager;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for Manager Scope Information
 * Contains all locations and permissions for a manager
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerScopeResponse {
    
    // Manager details
    private String managerId;
    private String managerName;
    private String managerEmail;
    private String managerRole;
    
    // Scope information
    private List<ScopeLocation> managedLocations;
    private List<String> permissions;
    
    // Accessibility stats
    private int totalSambhags;
    private int totalDistricts;
    private int totalBlocks;
    private int totalUsers;  // Users under management
    
    /**
     * Inner class for location scope details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScopeLocation {
        private UUID locationId;
        private String locationName;
        private String locationType;  // "SAMBHAG", "DISTRICT", "BLOCK"
        private String parentLocation; // Parent location name for context
        private int userCount;         // Number of users in this location
        
        // Hierarchy path
        private String fullPath;      // "Sambhag > District > Block"
    }
    
    /**
     * Check if manager has access to specific location
     */
    public boolean hasAccessToLocation(String locationType, UUID locationId) {
        return managedLocations.stream()
                .anyMatch(loc -> loc.getLocationType().equals(locationType) 
                               && loc.getLocationId().equals(locationId));
    }
    
    /**
     * Get total managed area summary
     */
    public String getManagedAreaSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (totalSambhags > 0) {
            summary.append(totalSambhags).append(" Sambhag(s)");
        }
        
        if (totalDistricts > 0) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(totalDistricts).append(" District(s)");
        }
        
        if (totalBlocks > 0) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(totalBlocks).append(" Block(s)");
        }
        
        return summary.toString();
    }
}