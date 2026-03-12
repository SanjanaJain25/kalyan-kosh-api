package com.example.kalyan_kosh_api.dto.manager;

import com.example.kalyan_kosh_api.entity.ManagerLevel;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * DTO for Manager Assignment Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerAssignmentResponse {
    private Long id;
    private String managerId;
    private String managerName;
    private String managerEmail;
    private ManagerLevel managerLevel;
    
    // Location details
    private String sambhagId;
    private String sambhagName;
    private String districtId;
    private String districtName;
    private String blockId;
    private String blockName;
    
    // Assignment metadata
    private boolean isActive;
    private Instant assignedAt;
    private String assignedByName;
    private String notes;
    
    // Computed fields
    private String locationDisplay;  // "Sambhag: XYZ" or "District: ABC" etc.
    private String fullLocationPath;  // "State > Sambhag > District > Block"
    
    /**
     * Helper method to generate location display string
     */
    public String getLocationDisplay() {
        if (blockName != null) {
            return "Block: " + blockName;
        } else if (districtName != null) {
            return "District: " + districtName;
        } else if (sambhagName != null) {
            return "Sambhag: " + sambhagName;
        }
        return "No specific location";
    }
    
    /**
     * Helper method to generate full location path
     */
    public String getFullLocationPath() {
        StringBuilder path = new StringBuilder();
        
        if (sambhagName != null) {
            path.append(sambhagName);
        }
        
        if (districtName != null) {
            if (path.length() > 0) path.append(" > ");
            path.append(districtName);
        }
        
        if (blockName != null) {
            if (path.length() > 0) path.append(" > ");
            path.append(blockName);
        }
        
        return path.toString();
    }
}