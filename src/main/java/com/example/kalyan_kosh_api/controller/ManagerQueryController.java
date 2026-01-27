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

import jakarta.validation.Valid;
import java.util.Map;

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
    
    /**
     * Create new query
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getQueries(
            @RequestParam(required = false) QueryStatus status,
            @RequestParam(required = false) QueryPriority priority,
            @RequestParam(required = false, defaultValue = "assigned") String type, // "assigned", "created", "all"
            Pageable pageable,
            Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            Page<ManagerQueryResponse> queries;
            if ("created".equals(type)) {
                queries = managerQueryService.getCreatedQueries(manager, pageable);
            } else {
                queries = managerQueryService.getQueriesForManager(manager, status, priority, pageable);
            }
            
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get query statistics for dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getQuery(@PathVariable Long queryId,
                                     Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            // This would need additional authorization logic to ensure manager can access this query
            // For now, we'll use a placeholder approach
            return ResponseEntity.ok(Map.of("message", "Query details would be returned here"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ QUERY ACTION ENDPOINTS ============
    
    /**
     * Assign query to manager
     */
    @PutMapping("/{queryId}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER')")
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