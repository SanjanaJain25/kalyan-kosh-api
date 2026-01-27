package com.example.kalyan_kosh_api.dto.manager;

import com.example.kalyan_kosh_api.entity.QueryPriority;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO for creating new manager queries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateManagerQueryRequest {
    
    @NotBlank(message = "Query title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Query description is required")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @NotNull(message = "Priority is required")
    private QueryPriority priority = QueryPriority.MEDIUM;
    
    // Optional: Related location context
    private UUID relatedSambhagId;
    private UUID relatedDistrictId;
    private UUID relatedBlockId;

    // Optional: Related user context
    private String relatedUserId;
    
    // Optional: Specific assignment (if known)
    private String assignToManagerId;
}