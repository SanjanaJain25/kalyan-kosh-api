package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.DeleteRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeleteRequestResponse {
    private Long id;
    private String entityType;
    private String entityId;
    private String requestedBy;
    private String requestedByName;
    private String requestedByRole;
    private String requestedFromDashboard;
    private String reason;
    private String status;
    private String approvalLevel;
    private String approvedBy;
    private Instant approvedAt;
    private String rejectedBy;
    private Instant rejectedAt;
    private String rejectionReason;
    private Instant executedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static DeleteRequestResponse fromEntity(DeleteRequest request) {
        return DeleteRequestResponse.builder()
                .id(request.getId())
                .entityType(request.getEntityType() != null ? request.getEntityType().name() : null)
                .entityId(request.getEntityId())
                .requestedBy(request.getRequestedBy() != null ? request.getRequestedBy().getId() : null)
                .requestedByName(request.getRequestedBy() != null ? request.getRequestedBy().getName() : null)
                .requestedByRole(request.getRequestedByRole() != null ? request.getRequestedByRole().name() : null)
                .requestedFromDashboard(request.getRequestedFromDashboard())
                .reason(request.getReason())
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .approvalLevel(request.getApprovalLevel())
                .approvedBy(request.getApprovedBy() != null ? request.getApprovedBy().getId() : null)
                .approvedAt(request.getApprovedAt())
                .rejectedBy(request.getRejectedBy() != null ? request.getRejectedBy().getId() : null)
                .rejectedAt(request.getRejectedAt())
                .rejectionReason(request.getRejectionReason())
                .executedAt(request.getExecutedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}