package com.example.kalyan_kosh_api.dto.manager;

import com.example.kalyan_kosh_api.entity.ManagerLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating new manager assignments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateManagerAssignmentRequest {
    
    @NotNull(message = "Manager ID is required")
    private String managerId;
    
    @NotNull(message = "Manager level is required")
    private ManagerLevel managerLevel;
    
    // Location assignments - at least one should be provided based on manager level
    private String sambhagId;
    private String districtId;
    private String blockId;
    
    // Optional assignment details
    private String notes;
    
    /**
     * Validation method to ensure correct location assignment based on manager level
     */
    public boolean isValidAssignment() {
        switch (managerLevel) {
            case SAMBHAG:
                return sambhagId != null && districtId == null && blockId == null;
            case DISTRICT:
                return districtId != null && blockId == null;
            case BLOCK:
                return blockId != null;
            default:
                return false;
        }
    }
    
    /**
     * Get the appropriate location name for validation error messages
     */
    public String getExpectedLocationLevel() {
        switch (managerLevel) {
            case SAMBHAG:
                return "sambhagId";
            case DISTRICT:
                return "districtId";
            case BLOCK:
                return "blockId";
            default:
                return "unknown";
        }
    }
}