package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.*;
import com.example.kalyan_kosh_api.dto.manager.ManagerScopeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kalyan_kosh_api.dto.manager.ManagerAreaScope;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Manager Scope and Access Control
 * Determines what locations and users a manager can access
 */
@Service
@Transactional(readOnly = true)
public class ManagerScopeService {
    
    @Autowired
    private ManagerAssignmentRepository managerAssignmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get complete scope information for a manager
     */
    public ManagerScopeResponse getManagerScope(User manager) {
        List<ManagerAssignment> assignments = managerAssignmentRepository.findByManagerAndIsActiveTrue(manager);
        
        if (assignments.isEmpty()) {
            return ManagerScopeResponse.builder()
                .managerId(manager.getId())
                .managerName(manager.getName())
                .managerEmail(manager.getEmail())
                .managerRole(manager.getRole().toString())
                .managedLocations(new ArrayList<>())
                .permissions(getManagerPermissions(manager.getRole()))
                .totalSambhags(0)
                .totalDistricts(0)
                .totalBlocks(0)
                .totalUsers(0)
                .build();
        }
        
        // Build scope locations
        List<ManagerScopeResponse.ScopeLocation> scopeLocations = assignments.stream()
            .map(this::mapToScopeLocation)
            .collect(Collectors.toList());
        
        // Calculate totals
        int totalSambhags = (int) assignments.stream().filter(a -> a.getSambhag() != null).count();
        int totalDistricts = (int) assignments.stream().filter(a -> a.getDistrict() != null).count();
        int totalBlocks = (int) assignments.stream().filter(a -> a.getBlock() != null).count();
        
        // Calculate total users under management (this would need more complex logic)
        int totalUsers = calculateManagedUsers(assignments);
        
        return ManagerScopeResponse.builder()
            .managerId(manager.getId())
            .managerName(manager.getName())
            .managerEmail(manager.getEmail())
            .managerRole(manager.getRole().toString())
            .managedLocations(scopeLocations)
            .permissions(getManagerPermissions(manager.getRole()))
            .totalSambhags(totalSambhags)
            .totalDistricts(totalDistricts)
            .totalBlocks(totalBlocks)
            .totalUsers(totalUsers)
            .build();
    }
    public boolean isAdminOrSuperAdmin(User user) {
    return user != null &&
            (user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_SUPERADMIN);
}

public boolean isManagerRole(Role role) {
    return role == Role.ROLE_SAMBHAG_MANAGER
            || role == Role.ROLE_DISTRICT_MANAGER
            || role == Role.ROLE_BLOCK_MANAGER;
}

public boolean isManager(User user) {
    return user != null && isManagerRole(user.getRole());
}

public ManagerAreaScope buildAreaScope(User currentUser) {
    if (isAdminOrSuperAdmin(currentUser)) {
        return ManagerAreaScope.builder()
                .unrestricted(true)
                .sambhagIds(List.of("__ALL__"))
                .districtIds(List.of("__ALL__"))
                .blockIds(List.of("__ALL__"))
                .build();
    }

    if (!isManager(currentUser)) {
        return ManagerAreaScope.builder()
                .unrestricted(false)
                .sambhagIds(List.of("__NO_SAMBHAG__"))
                .districtIds(List.of("__NO_DISTRICT__"))
                .blockIds(List.of("__NO_BLOCK__"))
                .build();
    }

    List<ManagerAssignment> assignments =
            managerAssignmentRepository.findByManagerAndIsActiveTrue(currentUser);

    List<String> sambhagIds = assignments.stream()
            .filter(a -> a.getSambhag() != null)
            .map(a -> a.getSambhag().getId().toString())
            .distinct()
            .toList();

    List<String> districtIds = assignments.stream()
            .filter(a -> a.getDistrict() != null)
            .map(a -> a.getDistrict().getId().toString())
            .distinct()
            .toList();

    List<String> blockIds = assignments.stream()
            .filter(a -> a.getBlock() != null)
            .map(a -> a.getBlock().getId().toString())
            .distinct()
            .toList();

    if (sambhagIds.isEmpty()) {
        sambhagIds = List.of("__NO_SAMBHAG__");
    }

    if (districtIds.isEmpty()) {
        districtIds = List.of("__NO_DISTRICT__");
    }

    if (blockIds.isEmpty()) {
        blockIds = List.of("__NO_BLOCK__");
    }

    return ManagerAreaScope.builder()
            .unrestricted(false)
            .sambhagIds(sambhagIds)
            .districtIds(districtIds)
            .blockIds(blockIds)
            .build();
}
    /**
     * Check if manager has access to specific sambhag
     */
    public boolean hasAccessToSambhag(User manager, UUID sambhagId) {
        return managerAssignmentRepository.hasAssignmentForSambhag(manager, sambhagId);
    }
    
    /**
     * Check if manager has access to specific district
     */
    public boolean hasAccessToDistrict(User manager, UUID districtId) {
    if (isAdminOrSuperAdmin(manager)) {
        return true;
    }

    if (managerAssignmentRepository.hasAssignmentForDistrict(manager, districtId)) {
        return true;
    }

    List<ManagerAssignment> assignments =
            managerAssignmentRepository.findByManagerAndIsActiveTrue(manager);

    return assignments.stream().anyMatch(a ->
            a.getSambhag() != null
                    && a.getDistrict() == null
                    && a.getBlock() == null
                    && a.getSambhag().getDistricts() != null
                    && a.getSambhag().getDistricts().stream()
                    .anyMatch(d -> d.getId().equals(districtId))
    );
}

public boolean hasAccessToBlock(User manager, UUID blockId) {
    if (isAdminOrSuperAdmin(manager)) {
        return true;
    }

    if (managerAssignmentRepository.hasAssignmentForBlock(manager, blockId)) {
        return true;
    }

    List<ManagerAssignment> assignments =
            managerAssignmentRepository.findByManagerAndIsActiveTrue(manager);

    return assignments.stream().anyMatch(a -> {
        if (a.getDistrict() != null && a.getBlock() == null) {
            return a.getDistrict().getBlocks() != null
                    && a.getDistrict().getBlocks().stream()
                    .anyMatch(b -> b.getId().equals(blockId));
        }

        if (a.getSambhag() != null && a.getDistrict() == null && a.getBlock() == null) {
            return a.getSambhag().getDistricts() != null
                    && a.getSambhag().getDistricts().stream()
                    .anyMatch(d -> d.getBlocks() != null
                            && d.getBlocks().stream()
                            .anyMatch(b -> b.getId().equals(blockId)));
        }

        return false;
    });
}
    /**
     * Get all sambhag IDs that manager has access to
     */
    public List<UUID> getAccessibleSambhagIds(User manager) {
        return managerAssignmentRepository.findSambhagIdsByManager(manager);
    }
    
    /**
     * Get all district IDs that manager has access to
     */
    public List<UUID> getAccessibleDistrictIds(User manager) {
        return managerAssignmentRepository.findDistrictIdsByManager(manager);
    }
    
    /**
     * Get all block IDs that manager has access to
     */
    public List<UUID> getAccessibleBlockIds(User manager) {
        return managerAssignmentRepository.findBlockIdsByManager(manager);
    }
    
    /**
     * Check if user can access specific user based on location hierarchy
     */
    public boolean canAccessUser(User manager, User targetUser) {
        // Admin can access everyone
        if (manager.getRole() == Role.ROLE_ADMIN) {
            return true;
        }
        
        // Self access
        if (manager.getId().equals(targetUser.getId())) {
            return true;
        }
        
        // Check based on target user's location (using department* fields from User entity)
        if (targetUser.getDepartmentBlock() != null && hasAccessToBlock(manager, targetUser.getDepartmentBlock().getId())) {
            return true;
        }
        
        if (targetUser.getDepartmentDistrict() != null && hasAccessToDistrict(manager, targetUser.getDepartmentDistrict().getId())) {
            return true;
        }
        
        if (targetUser.getDepartmentSambhag() != null && hasAccessToSambhag(manager, targetUser.getDepartmentSambhag().getId())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get manager permissions based on role
     */
    private List<String> getManagerPermissions(Role role) {
        List<String> permissions = new ArrayList<>();
        
        switch (role) {
            case ROLE_ADMIN:
                permissions.addAll(Arrays.asList(
                    "MANAGE_ALL_USERS", "MANAGE_ALL_MANAGERS", "VIEW_ALL_REPORTS", 
                    "MANAGE_ASSIGNMENTS", "RESOLVE_ALL_QUERIES", "EXPORT_DATA"
                ));
                break;
                
            case ROLE_SAMBHAG_MANAGER:
                permissions.addAll(Arrays.asList(
                    "VIEW_SAMBHAG_USERS", "MANAGE_DISTRICT_MANAGERS", "MANAGE_BLOCK_MANAGERS",
                    "VIEW_SAMBHAG_REPORTS", "RESOLVE_SAMBHAG_QUERIES", "CREATE_QUERIES"
                ));
                break;
                
            case ROLE_DISTRICT_MANAGER:
                permissions.addAll(Arrays.asList(
                    "VIEW_DISTRICT_USERS", "MANAGE_BLOCK_MANAGERS", 
                    "VIEW_DISTRICT_REPORTS", "RESOLVE_DISTRICT_QUERIES", "CREATE_QUERIES"
                ));
                break;
                
            case ROLE_BLOCK_MANAGER:
                permissions.addAll(Arrays.asList(
                    "VIEW_BLOCK_USERS", "VIEW_BLOCK_REPORTS", 
                    "RESOLVE_BLOCK_QUERIES", "CREATE_QUERIES"
                ));
                break;
                
            default:
                permissions.add("BASIC_ACCESS");
                break;
        }
        
        return permissions;
    }
    
    /**
     * Map assignment to scope location
     */
    private ManagerScopeResponse.ScopeLocation mapToScopeLocation(ManagerAssignment assignment) {
        String locationType;
        String locationName;
        UUID locationId;
        String parentLocation = "";
        String fullPath;
        
        if (assignment.getBlock() != null) {
            locationType = "BLOCK";
            locationName = assignment.getBlock().getName();
            locationId = assignment.getBlock().getId();
            
            // Build full path: Sambhag > District > Block
            StringBuilder pathBuilder = new StringBuilder();
            if (assignment.getBlock().getDistrict() != null) {
                if (assignment.getBlock().getDistrict().getSambhag() != null) {
                    pathBuilder.append(assignment.getBlock().getDistrict().getSambhag().getName()).append(" > ");
                }
                pathBuilder.append(assignment.getBlock().getDistrict().getName()).append(" > ");
                parentLocation = assignment.getBlock().getDistrict().getName();
            }
            pathBuilder.append(locationName);
            fullPath = pathBuilder.toString();
            
        } else if (assignment.getDistrict() != null) {
            locationType = "DISTRICT";
            locationName = assignment.getDistrict().getName();
            locationId = assignment.getDistrict().getId();
            
            StringBuilder pathBuilder = new StringBuilder();
            if (assignment.getDistrict().getSambhag() != null) {
                pathBuilder.append(assignment.getDistrict().getSambhag().getName()).append(" > ");
                parentLocation = assignment.getDistrict().getSambhag().getName();
            }
            pathBuilder.append(locationName);
            fullPath = pathBuilder.toString();
            
        } else if (assignment.getSambhag() != null) {
            locationType = "SAMBHAG";
            locationName = assignment.getSambhag().getName();
            locationId = assignment.getSambhag().getId();
            fullPath = locationName;
            
        } else {
            locationType = "UNKNOWN";
            locationName = "Unknown Location";
            locationId = null;
            fullPath = "Unknown";
        }
        
        return ManagerScopeResponse.ScopeLocation.builder()
            .locationId(locationId)
            .locationName(locationName)
            .locationType(locationType)
            .parentLocation(parentLocation)
            .fullPath(fullPath)
            .userCount(0) // TODO: Calculate actual user count
            .build();
    }
    
    /**
     * Calculate total users under management (placeholder implementation)
     */
    private int calculateManagedUsers(List<ManagerAssignment> assignments) {
        // This would need more complex logic to count users in all managed locations
        // For now, return a placeholder
        return assignments.size() * 10; // Rough estimate
    }
}