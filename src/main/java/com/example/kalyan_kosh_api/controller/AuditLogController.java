package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.DeleteEntityType;
import com.example.kalyan_kosh_api.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/entity")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> getEntityLogs(
            @RequestParam DeleteEntityType entityType,
            @RequestParam String entityId
    ) {
        return ResponseEntity.ok(auditLogService.getEntityLogs(entityType, entityId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> getLogsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(userId));
    }
    @GetMapping
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public ResponseEntity<?> getAllLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {
    return ResponseEntity.ok(auditLogService.getAllLogs(page, size));
}
}