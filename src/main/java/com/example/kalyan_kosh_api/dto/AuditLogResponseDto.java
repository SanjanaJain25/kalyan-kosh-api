package com.example.kalyan_kosh_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    private Long id;
    private String entityType;
    private String entityId;
    private String actionType;
    private String performedById;
    private String performedByName;
    private String performedByRole;
    private String remarks;
    private String ipAddress;
    private Instant performedAt;
}