package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.util.List;
import com.example.kalyan_kosh_api.dto.AuditLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
public Page<AuditLogResponseDto> getAllLogs(int page, int size) {
    Page<AuditLog> logsPage = auditLogRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"))
    );

    return new PageImpl<>(
            logsPage.getContent().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()),
            logsPage.getPageable(),
            logsPage.getTotalElements()
    );
}
private AuditLogResponseDto mapToDto(AuditLog log) {
    String performedById = null;
    String performedByName = null;

    if (log.getPerformedBy() != null) {
        performedById = log.getPerformedBy().getId();

        String firstName = log.getPerformedBy().getName() != null ? log.getPerformedBy().getName() : "";
        String surname = log.getPerformedBy().getSurname() != null ? log.getPerformedBy().getSurname() : "";
        String fullName = (firstName + " " + surname).trim();

        performedByName = fullName.isEmpty() ? null : fullName;
    }

    return AuditLogResponseDto.builder()
            .id(log.getId())
            .entityType(log.getEntityType() != null ? log.getEntityType().name() : null)
            .entityId(log.getEntityId())
            .actionType(log.getActionType() != null ? log.getActionType().name() : null)
            .performedById(performedById)
            .performedByName(performedByName)
            .performedByRole(log.getPerformedByRole() != null ? log.getPerformedByRole().name() : null)
            .remarks(log.getRemarks())
            .ipAddress(log.getIpAddress())
            .performedAt(log.getPerformedAt())
            .build();
}
    public AuditLog saveLog(
            DeleteEntityType entityType,
            String entityId,
            AuditActionType actionType,
            String oldDataJson,
            String newDataJson,
            User performedBy,
            String remarks,
            String ipAddress
    ) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setActionType(actionType);
        log.setOldDataJson(oldDataJson);
        log.setNewDataJson(newDataJson);
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(performedBy != null ? performedBy.getRole() : null);
        log.setRemarks(remarks);
        log.setIpAddress(ipAddress);
        log.setPerformedAt(Instant.now());

        return auditLogRepository.save(log);
    }

    public List<AuditLogResponseDto> getEntityLogs(DeleteEntityType entityType, String entityId) {
    return auditLogRepository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(entityType, entityId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
}

   public List<AuditLogResponseDto> getLogsByUser(String userId) {
    return auditLogRepository.findByPerformedBy_IdOrderByPerformedAtDesc(userId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
}
}