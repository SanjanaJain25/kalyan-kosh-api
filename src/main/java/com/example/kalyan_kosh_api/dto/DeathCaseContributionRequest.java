package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DeathCaseContributionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Death Case ID is required")
    private Long deathCaseId;

    @NotNull(message = "Total contribution amount is required")
    @Positive(message = "Amount must be positive")
    private Double totalContributionAmount;

    @NotBlank(message = "Comment is required")
    private String comment;

    // Note: MultipartFile will be handled separately in the controller
    // as @RequestPart("file")
}
