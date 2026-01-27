package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.*;
import com.example.kalyan_kosh_api.dto.manager.*;
import com.example.kalyan_kosh_api.dto.AdminUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "*")
public class ManagerController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ManagerAssignmentService managerAssignmentService;
    
    @Autowired
    private ManagerUserService managerUserService;
    
    @Autowired
    private ManagerScopeService managerScopeService;

    // ============ MANAGER ASSIGNMENT ENDPOINTS ============
    
    /**
     * Create new manager assignment
     */
    @PostMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAssignment(@Valid @RequestBody CreateManagerAssignmentRequest request,
                                            Authentication authentication) {
        try {
            User assignedBy = getCurrentUser(authentication);
            ManagerAssignmentResponse assignment = managerAssignmentService.createAssignment(request, assignedBy);
            return ResponseEntity.ok(assignment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get manager assignments
     */
    @GetMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getManagerAssignments(@RequestParam(required = false) String managerId,
                                                  Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // If managerId not provided, get current user's assignments
            String targetManagerId = managerId != null ? managerId : currentUser.getId();
            
            // Check permission - can view own assignments or admin can view all
            if (!targetManagerId.equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            
            List<ManagerAssignmentResponse> assignments = managerAssignmentService.getManagerAssignments(targetManagerId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get assignments by location
     */
    @GetMapping("/assignments/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getAssignmentsByLocation(@RequestParam(required = false) UUID sambhagId,
                                                     @RequestParam(required = false) UUID districtId,
                                                     @RequestParam(required = false) UUID blockId,
                                                     Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Verify access to requested location
            if (currentUser.getRole() != Role.ROLE_ADMIN) {
                if ((sambhagId != null && !managerScopeService.hasAccessToSambhag(currentUser, sambhagId)) ||
                    (districtId != null && !managerScopeService.hasAccessToDistrict(currentUser, districtId)) ||
                    (blockId != null && !managerScopeService.hasAccessToBlock(currentUser, blockId))) {
                    return ResponseEntity.status(403).body(Map.of("error", "Access denied to this location"));
                }
            }
            
            List<ManagerAssignmentResponse> assignments = managerAssignmentService.getAssignmentsByLocation(
                sambhagId, districtId, blockId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Remove manager assignment
     */
    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeAssignment(@PathVariable Long assignmentId,
                                            Authentication authentication) {
        try {
            User removedBy = getCurrentUser(authentication);
            managerAssignmentService.removeAssignment(assignmentId, removedBy);
            return ResponseEntity.ok(Map.of("message", "Assignment removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ MANAGER SCOPE ENDPOINTS ============
    
    /**
     * Get manager scope and permissions
     */
    @GetMapping("/scope")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getManagerScope(@RequestParam(required = false) String managerId,
                                           Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Default to current user if no managerId provided
            String targetManagerId = managerId != null ? managerId : currentUser.getId();
            
            // Check permission
            if (!targetManagerId.equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            
            User targetManager = userRepository.findById(targetManagerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
            
            ManagerScopeResponse scope = managerScopeService.getManagerScope(targetManager);
            return ResponseEntity.ok(scope);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ MANAGER USER ACCESS ENDPOINTS ============
    
    /**
     * Get users accessible to manager with filtering
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getAccessibleUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String stateId,
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            Pageable pageable,
            Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            Page<AdminUserResponse> users = managerUserService.getAccessibleUsers(
                manager, search, role, status, stateId, sambhagId, districtId, blockId, pageable);
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get users by specific location
     */
    @GetMapping("/users/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getUsersByLocation(
            @RequestParam String locationType,
            @RequestParam UUID locationId,
            Pageable pageable,
            Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            
            Page<AdminUserResponse> users = managerUserService.getUsersByLocation(
                manager, locationType, locationId, pageable);
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get user statistics for manager
     */
    @GetMapping("/users/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> getUserStats(Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ManagerUserService.ManagerUserStats stats = managerUserService.getUserStats(manager);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Block user
     */
    @PutMapping("/users/{userId}/block")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> blockUser(@PathVariable String userId,
                                     @RequestBody Map<String, String> body,
                                     Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            String reason = body.get("reason");
            
            managerUserService.blockUser(manager, userId, reason);
            return ResponseEntity.ok(Map.of("message", "User blocked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Unblock user
     */
    @PutMapping("/users/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> unblockUser(@PathVariable String userId, Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            managerUserService.unblockUser(manager, userId);
            return ResponseEntity.ok(Map.of("message", "User unblocked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update user role
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER')")
    public ResponseEntity<?> updateUserRole(@PathVariable String userId,
                                          @RequestBody Map<String, String> body,
                                          Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            Role newRole = Role.valueOf(body.get("role"));
            
            managerUserService.updateUserRole(manager, userId, newRole);
            return ResponseEntity.ok(Map.of("message", "User role updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ============ LEGACY REPORTS ENDPOINT ============
    
    @PostMapping("/reports")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SAMBHAG_MANAGER') or hasRole('DISTRICT_MANAGER') or hasRole('BLOCK_MANAGER')")
    public ResponseEntity<?> managerReports(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            // Determine manager level for filtering
            String managerLevel = user.getRole() == Role.ROLE_SAMBHAG_MANAGER ? "SAMBHAG" :
                                  user.getRole() == Role.ROLE_DISTRICT_MANAGER ? "DISTRICT" :
                                  user.getRole() == Role.ROLE_BLOCK_MANAGER ? "BLOCK" : "ALL";

            return ResponseEntity.ok(Map.of(
                "message", "Manager report accessed",
                "managerLevel", managerLevel,
                "userId", user.getId()
            ));
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

