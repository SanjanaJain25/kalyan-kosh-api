package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeathCaseContributionResponse {

    private Long id;
    private String userId;
    private String userName;
    private Long deathCaseId;
    private Double totalContributionAmount;
    private String comment;
    private String receiptFileName;
    private String receiptUrl;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
