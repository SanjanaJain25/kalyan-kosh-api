package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.*;
import com.example.kalyan_kosh_api.dto.AdminUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.UUID;

/**
 * Service for location-filtered user management by managers
 * Provides user access based on manager's assigned locations
 */
@Service
@Transactional(readOnly = true)
public class ManagerUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ManagerScopeService managerScopeService;
    
    /**
     * Get users accessible to manager with filtering
     */
    public Page<AdminUserResponse> getAccessibleUsers(User manager, String searchTerm, 
                                                     Role filterRole, UserStatus filterStatus,
                                                     String filterStateId, String filterSambhagId,
                                                     String filterDistrictId, String filterBlockId,
                                                     Pageable pageable) {
        
        // Admin can see all users
        if (manager.getRole() == Role.ROLE_ADMIN || manager.getRole() == Role.ROLE_SUPERADMIN) {
            return getAllUsersWithFilters(searchTerm, filterRole, filterStatus, 
                                        filterStateId, filterSambhagId, filterDistrictId, filterBlockId, pageable);
        }
        
        // Get manager's accessible location IDs
        List<UUID> accessibleSambhagIds = managerScopeService.getAccessibleSambhagIds(manager);
        List<UUID> accessibleDistrictIds = managerScopeService.getAccessibleDistrictIds(manager);
        List<UUID> accessibleBlockIds = managerScopeService.getAccessibleBlockIds(manager);

        // Build specification with location-based filtering
        Specification<User> spec = buildManagerUserSpecification(
            manager, searchTerm, filterRole, filterStatus,
            filterStateId, filterSambhagId, filterDistrictId, filterBlockId,
            accessibleSambhagIds, accessibleDistrictIds, accessibleBlockIds
        );
        
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToAdminUserResponse);
    }
    
    /**
     * Check if manager can access specific user
     */
    public boolean canAccessUser(User manager, String userId) {
        if (manager.getRole() == Role.ROLE_ADMIN || manager.getRole() == Role.ROLE_SUPERADMIN) {
            return true;
        }
        
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return false;
        }
        
        return managerScopeService.canAccessUser(manager, targetUser);
    }
    
    /**
     * Get users count in manager's scope
     */
    public ManagerUserStats getUserStats(User manager) {
        if (manager.getRole() == Role.ROLE_ADMIN || manager.getRole() == Role.ROLE_SUPERADMIN) {
            // Admin sees global stats
            return getGlobalUserStats();
        }
        
// Get location-specific stats
        List<UUID> sambhagIds = managerScopeService.getAccessibleSambhagIds(manager);
        List<UUID> districtIds = managerScopeService.getAccessibleDistrictIds(manager);
        List<UUID> blockIds = managerScopeService.getAccessibleBlockIds(manager);

        return getLocationBasedStats(sambhagIds, districtIds, blockIds);
    }
    
    /**
     * Get users by specific location for manager
     */
    public Page<AdminUserResponse> getUsersByLocation(User manager, String locationType, 
                                                     UUID locationId, Pageable pageable) {

        // Verify manager has access to this location
        boolean hasAccess = false;
        switch (locationType.toUpperCase()) {
            case "SAMBHAG":
                hasAccess = managerScopeService.hasAccessToSambhag(manager, locationId);
                break;
            case "DISTRICT":
                hasAccess = managerScopeService.hasAccessToDistrict(manager, locationId);
                break;
            case "BLOCK":
                hasAccess = managerScopeService.hasAccessToBlock(manager, locationId);
                break;
        }
        
        if (!hasAccess && manager.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You don't have access to this location");
        }
        
        // Build location-specific query
        Specification<User> spec = buildLocationSpecification(locationType, locationId);
        Page<User> users = userRepository.findAll(spec, pageable);
        
        return users.map(this::mapToAdminUserResponse);
    }
    
    /**
     * Block user (if manager has permission)
     */
    @Transactional
    public void blockUser(User manager, String userId, String reason) {
        if (!canAccessUser(manager, userId)) {
            throw new IllegalArgumentException("You don't have access to this user");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Block user
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
    }
    
    /**
     * Unblock user (if manager has permission)
     */
    @Transactional
    public void unblockUser(User manager, String userId) {
        if (!canAccessUser(manager, userId)) {
            throw new IllegalArgumentException("You don't have access to this user");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
    
    /**
     * Update user role (if manager has permission)
     */
    @Transactional
    public void updateUserRole(User manager, String userId, Role newRole) {
        if (!canAccessUser(manager, userId)) {
            throw new IllegalArgumentException("You don't have access to this user");
        }
        
        // Check if manager can assign this role
        if (!canAssignRole(manager, newRole)) {
            throw new IllegalArgumentException("You don't have permission to assign this role");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setRole(newRole);
        userRepository.save(user);
    }
    
    /**
     * Build specification for manager-accessible users
     */
private Specification<User> buildManagerUserSpecification(
            User manager, String searchTerm, Role filterRole, UserStatus filterStatus,
            String filterStateId, String filterSambhagId, String filterDistrictId, String filterBlockId,
            List<UUID> accessibleSambhagIds, List<UUID> accessibleDistrictIds, List<UUID> accessibleBlockIds) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Location-based access control
            if (!accessibleSambhagIds.isEmpty() || !accessibleDistrictIds.isEmpty() || !accessibleBlockIds.isEmpty()) {
                List<Predicate> locationPredicates = new ArrayList<>();
                
                if (!accessibleSambhagIds.isEmpty()) {
                    locationPredicates.add(root.get("departmentSambhag").get("id").in(accessibleSambhagIds));
                }
                
                if (!accessibleDistrictIds.isEmpty()) {
                    locationPredicates.add(root.get("departmentDistrict").get("id").in(accessibleDistrictIds));
                }
                
                if (!accessibleBlockIds.isEmpty()) {
                    locationPredicates.add(root.get("departmentBlock").get("id").in(accessibleBlockIds));
                }
                
                // OR condition: user belongs to any accessible location
                predicates.add(criteriaBuilder.or(locationPredicates.toArray(new Predicate[0])));
            }
            
            // Apply standard filters
            addStandardFilters(predicates, root, criteriaBuilder, searchTerm, filterRole, 
                             filterStatus, filterStateId, filterSambhagId, filterDistrictId, filterBlockId);
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Build specification for location-based user query
     */
private Specification<User> buildLocationSpecification(String locationType, UUID locationId) {
        return (root, query, criteriaBuilder) -> {
            switch (locationType.toUpperCase()) {
                case "SAMBHAG":
                    return criteriaBuilder.equal(root.get("departmentSambhag").get("id"), locationId);
                case "DISTRICT":
                    return criteriaBuilder.equal(root.get("departmentDistrict").get("id"), locationId);
                case "BLOCK":
                    return criteriaBuilder.equal(root.get("departmentBlock").get("id"), locationId);
                default:
                    throw new IllegalArgumentException("Invalid location type: " + locationType);
            }
        };
    }
    
    /**
     * Add standard filtering predicates
     */
    private void addStandardFilters(List<Predicate> predicates, 
                                   jakarta.persistence.criteria.Root<User> root,
                                   jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                   String searchTerm, Role filterRole, UserStatus filterStatus,
                                   String filterStateId, String filterSambhagId,
                                   String filterDistrictId, String filterBlockId) {

        // Search term filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likePattern = "%" + searchTerm.trim().toLowerCase() + "%";
            Predicate namePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")), likePattern);
            Predicate emailPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("email")), likePattern);
            Predicate phonePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("mobileNumber")), likePattern);

            predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, phonePredicate));
        }
        
        // Role filter
        if (filterRole != null) {
            predicates.add(criteriaBuilder.equal(root.get("role"), filterRole));
        }
        
        // Status filter
        if (filterStatus != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), filterStatus));
        }
        
        // Location filters - convert String to UUID
        if (filterStateId != null && !filterStateId.isEmpty()) {
            UUID stateUUID = UUID.fromString(filterStateId);
            predicates.add(criteriaBuilder.equal(root.get("departmentState").get("id"), stateUUID));
        }
        
        if (filterSambhagId != null && !filterSambhagId.isEmpty()) {
            UUID sambhagUUID = UUID.fromString(filterSambhagId);
            predicates.add(criteriaBuilder.equal(root.get("departmentSambhag").get("id"), sambhagUUID));
        }
        
        if (filterDistrictId != null && !filterDistrictId.isEmpty()) {
            UUID districtUUID = UUID.fromString(filterDistrictId);
            predicates.add(criteriaBuilder.equal(root.get("departmentDistrict").get("id"), districtUUID));
        }
        
        if (filterBlockId != null && !filterBlockId.isEmpty()) {
            UUID blockUUID = UUID.fromString(filterBlockId);
            predicates.add(criteriaBuilder.equal(root.get("departmentBlock").get("id"), blockUUID));
        }
    }
    
    /**
     * Get all users with filters (for admin)
     */
    private Page<AdminUserResponse> getAllUsersWithFilters(String searchTerm, Role filterRole, 
                                                          UserStatus filterStatus, String filterStateId,
                                                          String filterSambhagId, String filterDistrictId,
                                                          String filterBlockId, Pageable pageable) {
        
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addStandardFilters(predicates, root, criteriaBuilder, searchTerm, filterRole, 
                             filterStatus, filterStateId, filterSambhagId, filterDistrictId, filterBlockId);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToAdminUserResponse);
    }
    
    /**
     * Get global user statistics
     */
   private ManagerUserStats getGlobalUserStats() {
    Specification<User> totalSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), Role.ROLE_USER),
                    criteriaBuilder.notEqual(root.get("status"), UserStatus.DELETED)
            );

    Specification<User> activeSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), Role.ROLE_USER),
                    criteriaBuilder.equal(root.get("status"), UserStatus.ACTIVE)
            );

    Specification<User> blockedSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), Role.ROLE_USER),
                    criteriaBuilder.equal(root.get("status"), UserStatus.BLOCKED)
            );

    Specification<User> deletedSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), Role.ROLE_USER),
                    criteriaBuilder.equal(root.get("status"), UserStatus.DELETED)
            );

    long totalUsers = userRepository.count(totalSpec);
    long activeUsers = userRepository.count(activeSpec);
    long blockedUsers = userRepository.count(blockedSpec);
    long deletedUsers = userRepository.count(deletedSpec);

    return ManagerUserStats.builder()
            .totalUsers((int) totalUsers)
            .activeUsers((int) activeUsers)
            .blockedUsers((int) blockedUsers)
            .deletedUsers((int) deletedUsers)
            .build();
}
    
    /**
     * Get location-based user statistics
     */
private ManagerUserStats getLocationBasedStats(
        List<UUID> sambhagIds,
        List<UUID> districtIds,
        List<UUID> blockIds
) {
    Specification<User> baseSpec = buildStatsSpecification(
            sambhagIds,
            districtIds,
            blockIds,
            null
    );

    Specification<User> activeSpec = buildStatsSpecification(
            sambhagIds,
            districtIds,
            blockIds,
            UserStatus.ACTIVE
    );

    Specification<User> blockedSpec = buildStatsSpecification(
            sambhagIds,
            districtIds,
            blockIds,
            UserStatus.BLOCKED
    );

    Specification<User> deletedSpec = buildStatsSpecification(
            sambhagIds,
            districtIds,
            blockIds,
            UserStatus.DELETED
    );

    long totalUsers = userRepository.count(baseSpec);
    long activeUsers = userRepository.count(activeSpec);
    long blockedUsers = userRepository.count(blockedSpec);
    long deletedUsers = userRepository.count(deletedSpec);

    return ManagerUserStats.builder()
            .totalUsers((int) totalUsers)
            .activeUsers((int) activeUsers)
            .blockedUsers((int) blockedUsers)
            .deletedUsers((int) deletedUsers)
            .build();
}    

private Specification<User> buildStatsSpecification(
        List<UUID> sambhagIds,
        List<UUID> districtIds,
        List<UUID> blockIds,
        UserStatus status
) {
    return (root, query, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();

        // Count only normal members, not managers/admins
        predicates.add(criteriaBuilder.equal(root.get("role"), Role.ROLE_USER));

        if (status != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
        } else {
            // Total users should not include deleted users
            predicates.add(criteriaBuilder.notEqual(root.get("status"), UserStatus.DELETED));
        }

        List<Predicate> locationPredicates = new ArrayList<>();

        if (sambhagIds != null && !sambhagIds.isEmpty()) {
            locationPredicates.add(root.get("departmentSambhag").get("id").in(sambhagIds));
        }

        if (districtIds != null && !districtIds.isEmpty()) {
            locationPredicates.add(root.get("departmentDistrict").get("id").in(districtIds));
        }

        if (blockIds != null && !blockIds.isEmpty()) {
            locationPredicates.add(root.get("departmentBlock").get("id").in(blockIds));
        }

        if (!locationPredicates.isEmpty()) {
            predicates.add(criteriaBuilder.or(locationPredicates.toArray(new Predicate[0])));
        } else {
            // Manager has no assigned area, so count should be 0
            predicates.add(criteriaBuilder.disjunction());
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
}
    /**
     * Check if manager can assign specific role
     */
    private boolean canAssignRole(User manager, Role targetRole) {
        Role managerRole = manager.getRole();
        
        // Admin can assign any role
        if (managerRole == Role.ROLE_ADMIN) {
            return true;
        }
        
        // Managers can only assign roles below their level
        int managerLevel = getRoleLevel(managerRole);
        int targetLevel = getRoleLevel(targetRole);
        
        return managerLevel > targetLevel;
    }
    
    /**
     * Get role hierarchy level
     */
    private int getRoleLevel(Role role) {
        switch (role) {
            case ROLE_ADMIN: return 4;
            case ROLE_SAMBHAG_MANAGER: return 3;
            case ROLE_DISTRICT_MANAGER: return 2;
            case ROLE_BLOCK_MANAGER: return 1;
            case ROLE_USER: return 0;
            default: return -1;
        }
    }
    
    /**
     * Map User entity to AdminUserResponse
     */
    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .surname(user.getSurname())
            .fatherName(user.getFatherName())
            .email(user.getEmail())
            .mobileNumber(user.getMobileNumber())
            .dateOfBirth(user.getDateOfBirth())
            .role(user.getRole())
            .status(user.getStatus())
            .departmentState(user.getDepartmentState() != null ? user.getDepartmentState().getName() : null)
            .departmentSambhag(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : null)
            .departmentDistrict(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : null)
            .departmentBlock(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : null)
            .department(user.getDepartment())
            .departmentUniqueId(user.getDepartmentUniqueId())
            .schoolOfficeName(user.getSchoolOfficeName())
            .sankulName(user.getSankulName())
            .homeAddress(user.getHomeAddress())
            .pincode(user.getPincode())
            .joiningDate(user.getJoiningDate())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
    
    /**
     * DTO for user statistics
     */
    public static class ManagerUserStats {
        private int totalUsers;
        private int activeUsers;
        private int blockedUsers;
        private int deletedUsers;
        
        // Builder pattern implementation
        public static ManagerUserStatsBuilder builder() {
            return new ManagerUserStatsBuilder();
        }
        
        public static class ManagerUserStatsBuilder {
            private int totalUsers;
            private int activeUsers;
            private int blockedUsers;
            private int deletedUsers;
            
            public ManagerUserStatsBuilder totalUsers(int totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }
            
            public ManagerUserStatsBuilder activeUsers(int activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }
            
            public ManagerUserStatsBuilder blockedUsers(int blockedUsers) {
                this.blockedUsers = blockedUsers;
                return this;
            }
            
            public ManagerUserStatsBuilder deletedUsers(int deletedUsers) {
                this.deletedUsers = deletedUsers;
                return this;
            }
            
            public ManagerUserStats build() {
                ManagerUserStats stats = new ManagerUserStats();
                stats.totalUsers = this.totalUsers;
                stats.activeUsers = this.activeUsers;
                stats.blockedUsers = this.blockedUsers;
                stats.deletedUsers = this.deletedUsers;
                return stats;
            }
        }
        
        // Getters
        public int getTotalUsers() { return totalUsers; }
        public int getActiveUsers() { return activeUsers; }
        public int getBlockedUsers() { return blockedUsers; }
        public int getDeletedUsers() { return deletedUsers; }
    }
}