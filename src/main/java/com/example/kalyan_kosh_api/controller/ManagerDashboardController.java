package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.*;
import com.example.kalyan_kosh_api.dto.manager.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Manager Dashboard Overview
 * Provides consolidated dashboard data and statistics
 */
@RestController
@RequestMapping("/api/manager/dashboard")
@CrossOrigin(origins = "*")
public class ManagerDashboardController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ManagerScopeService managerScopeService;
    
    @Autowired
    private ManagerQueryService managerQueryService;
    
    @Autowired
    private ManagerUserService managerUserService;
    
    @Autowired
    private ManagerAssignmentService managerAssignmentService;

    /**
     * Get complete dashboard overview for manager
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getDashboardOverview(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            // Get manager scope and permissions
            ManagerScopeResponse scope = managerScopeService.getManagerScope(manager);
            
            // Get query statistics
            ManagerQueryService.ManagerQueryStats queryStats = managerQueryService.getQueryStats(manager);
            
            // Get user statistics
            ManagerUserService.ManagerUserStats userStats = managerUserService.getUserStats(manager);
            
            // Build dashboard response
            Map<String, Object> dashboard = new HashMap<>();

            // Manager info - null safe
            Map<String, Object> managerInfo = new HashMap<>();
            managerInfo.put("id", manager.getId() != null ? manager.getId() : "");
            managerInfo.put("name", manager.getName() != null ? manager.getName() : "");
            managerInfo.put("email", manager.getEmail() != null ? manager.getEmail() : "");
            managerInfo.put("role", manager.getRole() != null ? manager.getRole().toString() : "");
            dashboard.put("manager", managerInfo);

            dashboard.put("scope", scope);
            dashboard.put("queryStats", queryStats);
            dashboard.put("userStats", userStats);
            
            // Add quick summary with null-safe values
            Map<String, Object> summary = new HashMap<>();
            summary.put("managedLocations", scope != null && scope.getManagedLocations() != null ? scope.getManagedLocations().size() : 0);
            summary.put("totalUsers", userStats != null ? userStats.getTotalUsers() : 0);
            summary.put("pendingQueries", queryStats != null ? queryStats.getPendingCount() : 0);
            summary.put("urgentQueries", queryStats != null ? queryStats.getOverdueCount() : 0);
            dashboard.put("summary", summary);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get manager scope information
     */
    @GetMapping("/scope")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getScope(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ManagerScopeResponse scope = managerScopeService.getManagerScope(manager);
            return ResponseEntity.ok(scope);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get quick statistics summary
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getQuickStats(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            // Get all statistics
            ManagerQueryService.ManagerQueryStats queryStats = managerQueryService.getQueryStats(manager);
            ManagerUserService.ManagerUserStats userStats = managerUserService.getUserStats(manager);
            ManagerScopeResponse scope = managerScopeService.getManagerScope(manager);
            
            // Build consolidated stats
            Map<String, Object> stats = new HashMap<>();
            
            // Query statistics - null safe
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("pending", queryStats != null ? queryStats.getPendingCount() : 0);
            queryMap.put("inProgress", queryStats != null ? queryStats.getInProgressCount() : 0);
            queryMap.put("resolved", queryStats != null ? queryStats.getResolvedCount() : 0);
            queryMap.put("overdue", queryStats != null ? queryStats.getOverdueCount() : 0);
            queryMap.put("totalAssigned", queryStats != null ? queryStats.getTotalAssigned() : 0);
            stats.put("queries", queryMap);

            // User statistics - null safe
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("total", userStats != null ? userStats.getTotalUsers() : 0);
            userMap.put("active", userStats != null ? userStats.getActiveUsers() : 0);
            userMap.put("blocked", userStats != null ? userStats.getBlockedUsers() : 0);
            userMap.put("deleted", userStats != null ? userStats.getDeletedUsers() : 0);
            stats.put("users", userMap);

            // Scope statistics - null safe
            Map<String, Object> scopeMap = new HashMap<>();
            scopeMap.put("sambhags", scope != null ? scope.getTotalSambhags() : 0);
            scopeMap.put("districts", scope != null ? scope.getTotalDistricts() : 0);
            scopeMap.put("blocks", scope != null ? scope.getTotalBlocks() : 0);
            scopeMap.put("managedLocations", scope != null && scope.getManagedLocations() != null ? scope.getManagedLocations().size() : 0);
            stats.put("scope", scopeMap);

            // Performance indicators
            Map<String, Object> perfMap = new HashMap<>();
            perfMap.put("queryResolutionRate", queryStats != null ? calculateResolutionRate(queryStats) : 0.0);
            perfMap.put("averageResponseTime", "N/A");
            perfMap.put("userSatisfactionScore", "N/A");
            stats.put("performance", perfMap);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get manager alerts and notifications
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getAlerts(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ManagerQueryService.ManagerQueryStats queryStats = managerQueryService.getQueryStats(manager);
            
            // Build alerts based on statistics
            Map<String, Object> alerts = new HashMap<>();
            
            if (queryStats != null && queryStats.getOverdueCount() > 0) {
                Map<String, Object> overdueAlert = new HashMap<>();
                overdueAlert.put("count", queryStats.getOverdueCount());
                overdueAlert.put("message", "You have " + queryStats.getOverdueCount() + " overdue queries requiring attention");
                overdueAlert.put("severity", "high");
                overdueAlert.put("action", "/api/manager/queries?status=PENDING");
                alerts.put("overdueQueries", overdueAlert);
            }
            
            if (queryStats != null && queryStats.getPendingCount() > 5) {
                Map<String, Object> pendingAlert = new HashMap<>();
                pendingAlert.put("count", queryStats.getPendingCount());
                pendingAlert.put("message", "High number of pending queries: " + queryStats.getPendingCount());
                pendingAlert.put("severity", "medium");
                pendingAlert.put("action", "/api/manager/queries/pending");
                alerts.put("highPendingLoad", pendingAlert);
            }
            
            // Add more alert conditions as needed
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get recent activity summary
     */
    @GetMapping("/activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getRecentActivity(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            // This would typically fetch recent activities from various services
            // For now, return a placeholder structure
            Map<String, Object> activity = new HashMap<>();
            
            activity.put("recentQueries", "Recent queries would be listed here");
            activity.put("recentAssignments", "Recent assignments would be listed here");
            activity.put("recentUserActions", "Recent user management actions would be listed here");
            
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get manager permissions and capabilities
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getPermissions(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ManagerScopeResponse scope = managerScopeService.getManagerScope(manager);
            
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("role", manager.getRole() != null ? manager.getRole().toString() : "");
            permissions.put("permissions", scope != null ? scope.getPermissions() : new java.util.ArrayList<>());
            permissions.put("canManageUsers", canManageUsers(manager.getRole()));
            permissions.put("canAssignQueries", canAssignQueries(manager.getRole()));
            permissions.put("canEscalateQueries", canEscalateQueries(manager.getRole()));
            permissions.put("canViewAllReports", canViewAllReports(manager.getRole()));
            
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
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
    
    /**
     * Calculate query resolution rate
     */
    private double calculateResolutionRate(ManagerQueryService.ManagerQueryStats stats) {
        int total = stats.getPendingCount() + stats.getInProgressCount() + stats.getResolvedCount();
        if (total == 0) return 0.0;
        return (double) stats.getResolvedCount() / total * 100;
    }
    
    /**
     * Check if role can manage users
     */
    private boolean canManageUsers(Role role) {
        return role == Role.ROLE_ADMIN || 
               role == Role.ROLE_SAMBHAG_MANAGER || 
               role == Role.ROLE_DISTRICT_MANAGER ||
               role == Role.ROLE_BLOCK_MANAGER;
    }
    
    /**
     * Check if role can assign queries
     */
    private boolean canAssignQueries(Role role) {
        return role == Role.ROLE_ADMIN || 
               role == Role.ROLE_SAMBHAG_MANAGER || 
               role == Role.ROLE_DISTRICT_MANAGER;
    }
    
    /**
     * Check if role can escalate queries
     */
    private boolean canEscalateQueries(Role role) {
        return role == Role.ROLE_SAMBHAG_MANAGER || 
               role == Role.ROLE_DISTRICT_MANAGER ||
               role == Role.ROLE_BLOCK_MANAGER;
    }
    
    /**
     * Check if role can view all reports
     */
    private boolean canViewAllReports(Role role) {
        return role == Role.ROLE_ADMIN;
    }
}

