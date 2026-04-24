package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.ManagerQueryService;
import com.example.kalyan_kosh_api.dto.manager.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import java.util.Map;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
/**
 * Controller for Manager Query System
 * Handles query creation, assignment, resolution, and escalation
 */
@RestController
@RequestMapping("/api/manager/queries")
@CrossOrigin(origins = "*")
public class ManagerQueryController {

    @Autowired
    private ManagerQueryService managerQueryService;
    
    @Autowired
    private UserRepository userRepository;

    // ============ QUERY MANAGEMENT ENDPOINTS ============
    @GetMapping
@PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
public ResponseEntity<?> getQueries(
        @RequestParam(required = false) Long ticketId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) QueryStatus status,
        @RequestParam(required = false) QueryPriority priority,
        @RequestParam(required = false) String createdById,
        @RequestParam(required = false) String relatedUserId,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(defaultValue = "assigned") String type,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication
) {
    try {
        User currentUser = getCurrentUser(authentication);

        String cleanType = type == null || type.isBlank()
                ? "assigned"
                : type.toLowerCase();

        if ("all".equalsIgnoreCase(cleanType)) {
            boolean isAdmin =
                    currentUser.getRole() == Role.ROLE_ADMIN ||
                    currentUser.getRole() == Role.ROLE_SUPERADMIN;

            if (!isAdmin) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Only admin can view all tickets"));
            }
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String safeSortBy = switch (sortBy) {
            case "id", "title", "createdAt", "updatedAt", "priority", "status" -> sortBy;
            default -> "createdAt";
        };

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, safeSortBy)
        );

        Page<ManagerQueryResponse> queries = managerQueryService.searchTickets(
                currentUser,
                cleanType,
                ticketId,
                search,
                status,
                priority,
                createdById,
                relatedUserId,
                parseStartDate(fromDate),
                parseEndDate(toDate),
                pageable
        );

        return ResponseEntity.ok(queries);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
private Instant parseStartDate(String value) {
    if (value == null || value.trim().isEmpty()) {
        return null;
    }

    return LocalDate.parse(value.trim())
            .atStartOfDay(ZoneId.of("Asia/Kolkata"))
            .toInstant();
}

private Instant parseEndDate(String value) {
    if (value == null || value.trim().isEmpty()) {
        return null;
    }

    return LocalDate.parse(value.trim())
            .atTime(23, 59, 59)
            .atZone(ZoneId.of("Asia/Kolkata"))
            .toInstant();
}
    /**
     * Create new query
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> createQuery(@Valid @RequestBody CreateManagerQueryRequest request,
                                        Authentication authentication) {
        try {
            User createdBy = getCurrentUser(authentication);
            ManagerQueryResponse query = managerQueryService.createQuery(request, createdBy);
            return ResponseEntity.ok(query);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get queries for manager dashboard
     */
 
    
    /**
     * Get query statistics for dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getQueryStats(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ManagerQueryService.ManagerQueryStats stats = managerQueryService.getQueryStats(manager);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get specific query details
     */
    @GetMapping("/{queryId}")
@PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
public ResponseEntity<?> getQuery(@PathVariable Long queryId,
                                  Authentication authentication) {
    try {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(managerQueryService.getQueryById(queryId, currentUser));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

@GetMapping("/{queryId}/messages")
@PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
public ResponseEntity<?> getMessages(@PathVariable Long queryId,
                                     Authentication authentication) {
    try {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(managerQueryService.getMessages(queryId, currentUser));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
@PostMapping("/{queryId}/messages")
@PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
public ResponseEntity<?> addMessage(@PathVariable Long queryId,
                                    @Valid @RequestBody AddManagerQueryMessageRequest request,
                                    Authentication authentication) {
    try {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(
                managerQueryService.addMessage(queryId, request.getMessage(), currentUser)
        );
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
    
    // ============ QUERY ACTION ENDPOINTS ============
    
    /**
     * Assign query to manager
     */
    @PutMapping("/{queryId}/assign")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER')")
    public ResponseEntity<?> assignQuery(@PathVariable Long queryId,
                                        @RequestBody Map<String, String> body,
                                        Authentication authentication) {
        try {
            User assignedBy = getCurrentUser(authentication);
            String managerId = body.get("managerId");
            
            ManagerQueryResponse query = managerQueryService.assignQuery(queryId, managerId, assignedBy);
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update query status
     */
    @PutMapping("/{queryId}/status")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> updateQueryStatus(@PathVariable Long queryId,
                                              @RequestBody Map<String, String> body,
                                              Authentication authentication) {
        try {
            User updatedBy = getCurrentUser(authentication);
            QueryStatus newStatus = QueryStatus.valueOf(body.get("status"));
            
            ManagerQueryResponse query = managerQueryService.updateQueryStatus(queryId, newStatus, updatedBy);
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Resolve query with resolution text
     */
    @PutMapping("/{queryId}/resolve")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> resolveQuery(@PathVariable Long queryId,
                                         @RequestBody Map<String, String> body,
                                         Authentication authentication) {
        try {
            User resolvedBy = getCurrentUser(authentication);
            String resolution = body.get("resolution");
            
            if (resolution == null || resolution.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Resolution text is required"));
            }
            
            ManagerQueryResponse query = managerQueryService.resolveQuery(queryId, resolution, resolvedBy);
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Escalate query to higher level
     */
    @PutMapping("/{queryId}/escalate")
    @PreAuthorize("hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> escalateQuery(@PathVariable Long queryId,
                                          Authentication authentication) {
        try {
            User escalatedBy = getCurrentUser(authentication);
            ManagerQueryResponse query = managerQueryService.escalateQuery(queryId, escalatedBy);
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ QUERY FILTERING ENDPOINTS ============
    
    /**
     * Get pending queries that need immediate attention
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getPendingQueries(Authentication authentication, Pageable pageable) {
        try {
            User manager = getCurrentUser(authentication);
            Page<ManagerQueryResponse> queries = managerQueryService.getQueriesForManager(
                manager, QueryStatus.PENDING, null, pageable);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get urgent queries
     */
    @GetMapping("/urgent")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getUrgentQueries(Authentication authentication, Pageable pageable) {
        try {
            User manager = getCurrentUser(authentication);
            Page<ManagerQueryResponse> queries = managerQueryService.getQueriesForManager(
                manager, null, QueryPriority.URGENT, pageable);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get my created queries
     */
    @GetMapping("/my-queries")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getMyQueries(Authentication authentication, Pageable pageable) {
        try {
            User manager = getCurrentUser(authentication);
            Page<ManagerQueryResponse> queries = managerQueryService.getCreatedQueries(manager, pageable);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ BULK OPERATIONS ============
    
    /**
     * Bulk update query status
     */
    @PutMapping("/bulk/status")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER')")
    public ResponseEntity<?> bulkUpdateStatus(@RequestBody Map<String, Object> body,
                                             Authentication authentication) {
        try {
            User updatedBy = getCurrentUser(authentication);
            
            // This would need implementation for bulk operations
            return ResponseEntity.ok(Map.of("message", "Bulk status update would be implemented here"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ HELPER METHODS ============
    
    /**
     * Get current authenticated user
     */
    private User getCurrentUser(Authentication authentication) {
        String userIdStr = authentication.getName();
        return userRepository.findById(userIdStr)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}