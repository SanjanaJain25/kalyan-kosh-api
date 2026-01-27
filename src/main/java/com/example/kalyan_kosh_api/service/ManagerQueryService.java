package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.*;
import com.example.kalyan_kosh_api.dto.manager.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Manager Queries
 * Handles query creation, assignment, resolution, and escalation
 */
@Service
@Transactional
public class ManagerQueryService {
    
    @Autowired
    private ManagerQueryRepository managerQueryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ManagerScopeService managerScopeService;
    
    @Autowired
    private SambhagRepository sambhagRepository;
    
    @Autowired
    private DistrictRepository districtRepository;
    
    @Autowired
    private BlockRepository blockRepository;
    
    /**
     * Create new query
     */
    public ManagerQueryResponse createQuery(CreateManagerQueryRequest request, User createdBy) {
        ManagerQuery.ManagerQueryBuilder queryBuilder = ManagerQuery.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority())
            .createdBy(createdBy)
            .status(QueryStatus.PENDING);
        
        // Set related locations
        if (request.getRelatedSambhagId() != null) {
            Sambhag sambhag = sambhagRepository.findById(request.getRelatedSambhagId())
                .orElseThrow(() -> new IllegalArgumentException("Sambhag not found"));
            queryBuilder.relatedSambhag(sambhag);
        }
        
        if (request.getRelatedDistrictId() != null) {
            District district = districtRepository.findById(request.getRelatedDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found"));
            queryBuilder.relatedDistrict(district);
        }
        
        if (request.getRelatedBlockId() != null) {
            Block block = blockRepository.findById(request.getRelatedBlockId())
                .orElseThrow(() -> new IllegalArgumentException("Block not found"));
            queryBuilder.relatedBlock(block);
        }
        
        // Set related user if provided
        if (request.getRelatedUserId() != null) {
            User relatedUser = userRepository.findById(request.getRelatedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Related user not found"));
            queryBuilder.relatedUser(relatedUser);
        }
        
        // Auto-assign if manager specified
        if (request.getAssignToManagerId() != null) {
            User assignedTo = userRepository.findById(request.getAssignToManagerId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned manager not found"));
            queryBuilder.assignedTo(assignedTo);
        } else {
            // Auto-assign based on hierarchy and location
            User autoAssignedManager = findAppropriateManager(createdBy, request);
            if (autoAssignedManager != null) {
                queryBuilder.assignedTo(autoAssignedManager);
            }
        }
        
        ManagerQuery query = queryBuilder.build();
        ManagerQuery savedQuery = managerQueryRepository.save(query);
        
        return mapToResponse(savedQuery);
    }
    
    /**
     * Get queries for manager dashboard
     */
    @Transactional(readOnly = true)
    public Page<ManagerQueryResponse> getQueriesForManager(User manager, QueryStatus status, 
                                                          QueryPriority priority, Pageable pageable) {
        Page<ManagerQuery> queries;
        
        if (manager.getRole() == Role.ROLE_ADMIN) {
            // Admin sees all queries
            queries = managerQueryRepository.findWithFilters(null, null, status, priority, 
                                                            null, null, null, pageable);
        } else {
            // Manager sees assigned queries + queries in their scope
            // Commented out - types now UUID
            // List<UUID> sambhagIds = managerScopeService.getAccessibleSambhagIds(manager);
            // List<UUID> districtIds = managerScopeService.getAccessibleDistrictIds(manager);
            // List<UUID> blockIds = managerScopeService.getAccessibleBlockIds(manager);

            // For now, show assigned queries + location-based queries
            if (status != null || priority != null) {
                queries = managerQueryRepository.findWithFilters(null, manager, status, priority, 
                                                                null, null, null, pageable);
            } else {
                queries = managerQueryRepository.findByAssignedToOrderByCreatedAtDesc(manager, pageable);
            }
        }
        
        return queries.map(this::mapToResponse);
    }
    
    /**
     * Get queries created by manager
     */
    @Transactional(readOnly = true)
    public Page<ManagerQueryResponse> getCreatedQueries(User manager, Pageable pageable) {
        Page<ManagerQuery> queries = managerQueryRepository.findByCreatedByOrderByCreatedAtDesc(manager, pageable);
        return queries.map(this::mapToResponse);
    }
    
    /**
     * Assign query to manager
     */
    public ManagerQueryResponse assignQuery(Long queryId, String managerId, User assignedBy) {
        ManagerQuery query = managerQueryRepository.findById(queryId)
            .orElseThrow(() -> new IllegalArgumentException("Query not found"));
        
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
        
        // Validate assignment authority
        if (!canAssignQuery(assignedBy, query, manager)) {
            throw new IllegalArgumentException("You don't have authority to assign this query");
        }
        
        query.setAssignedTo(manager);
        query.setStatus(QueryStatus.PENDING);
        
        ManagerQuery savedQuery = managerQueryRepository.save(query);
        return mapToResponse(savedQuery);
    }
    
    /**
     * Update query status
     */
    public ManagerQueryResponse updateQueryStatus(Long queryId, QueryStatus newStatus, User updatedBy) {
        ManagerQuery query = managerQueryRepository.findById(queryId)
            .orElseThrow(() -> new IllegalArgumentException("Query not found"));
        
        // Validate status change authority
        if (!canUpdateQuery(updatedBy, query)) {
            throw new IllegalArgumentException("You don't have authority to update this query");
        }
        
        QueryStatus oldStatus = query.getStatus();
        query.setStatus(newStatus);
        
        // Set resolved by if marking as resolved
        if (newStatus == QueryStatus.RESOLVED && oldStatus != QueryStatus.RESOLVED) {
            query.setResolvedBy(updatedBy);
        }
        
        ManagerQuery savedQuery = managerQueryRepository.save(query);
        return mapToResponse(savedQuery);
    }
    
    /**
     * Add resolution to query
     */
    public ManagerQueryResponse resolveQuery(Long queryId, String resolution, User resolvedBy) {
        ManagerQuery query = managerQueryRepository.findById(queryId)
            .orElseThrow(() -> new IllegalArgumentException("Query not found"));
        
        if (!canUpdateQuery(resolvedBy, query)) {
            throw new IllegalArgumentException("You don't have authority to resolve this query");
        }
        
        query.setResolution(resolution);
        query.setStatus(QueryStatus.RESOLVED);
        query.setResolvedBy(resolvedBy);
        
        ManagerQuery savedQuery = managerQueryRepository.save(query);
        return mapToResponse(savedQuery);
    }
    
    /**
     * Escalate query to higher level
     */
    public ManagerQueryResponse escalateQuery(Long queryId, User escalatedBy) {
        ManagerQuery query = managerQueryRepository.findById(queryId)
            .orElseThrow(() -> new IllegalArgumentException("Query not found"));
        
        if (!canUpdateQuery(escalatedBy, query)) {
            throw new IllegalArgumentException("You don't have authority to escalate this query");
        }
        
        // Find higher level manager
        User higherManager = findHigherLevelManager(escalatedBy);
        if (higherManager == null) {
            throw new IllegalArgumentException("No higher level manager found for escalation");
        }
        
        query.setAssignedTo(higherManager);
        query.setStatus(QueryStatus.ESCALATED);
        query.setPriority(escalatePriority(query.getPriority()));
        
        ManagerQuery savedQuery = managerQueryRepository.save(query);
        return mapToResponse(savedQuery);
    }
    
    /**
     * Get query statistics for dashboard
     */
    @Transactional(readOnly = true)
    public ManagerQueryStats getQueryStats(User manager) {
        Long pendingCount = managerQueryRepository.countByStatusAndAssignedTo(QueryStatus.PENDING, manager);
        Long inProgressCount = managerQueryRepository.countByStatusAndAssignedTo(QueryStatus.IN_PROGRESS, manager);
        Long resolvedCount = managerQueryRepository.countByStatusAndAssignedTo(QueryStatus.RESOLVED, manager);
        
        // Find overdue queries (older than 7 days for regular, 3 days for urgent)
        Instant overdueRegular = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant overdueUrgent = Instant.now().minus(3, ChronoUnit.DAYS);
        List<ManagerQuery> overdueQueries = managerQueryRepository.findOverdueQueries(overdueRegular);
        
        // Filter overdue queries for this manager
        long overdueCount = overdueQueries.stream()
            .filter(q -> q.getAssignedTo() != null && q.getAssignedTo().getId().equals(manager.getId()))
            .count();
        
        return ManagerQueryStats.builder()
            .pendingCount(pendingCount.intValue())
            .inProgressCount(inProgressCount.intValue())
            .resolvedCount(resolvedCount.intValue())
            .overdueCount((int) overdueCount)
            .totalAssigned(pendingCount.intValue() + inProgressCount.intValue())
            .build();
    }
    
    /**
     * Auto-assign query based on hierarchy and location
     */
    private User findAppropriateManager(User creator, CreateManagerQueryRequest request) {
        // If query has location context, find manager for that location
        if (request.getRelatedBlockId() != null) {
            // Find block manager
            return findManagerForLocation("BLOCK", request.getRelatedBlockId());
        } else if (request.getRelatedDistrictId() != null) {
            // Find district manager
            return findManagerForLocation("DISTRICT", request.getRelatedDistrictId());
        } else if (request.getRelatedSambhagId() != null) {
            // Find sambhag manager
            return findManagerForLocation("SAMBHAG", request.getRelatedSambhagId());
        }
        
        // Default: escalate to immediate supervisor
        return findHigherLevelManager(creator);
    }
    
    /**
     * Find manager for specific location
     */
    private User findManagerForLocation(String locationType, UUID locationId) {
        // This would use ManagerAssignmentRepository to find appropriate manager
        // Simplified implementation for now
        return null; // TODO: Implement location-based manager lookup
    }
    
    /**
     * Find higher level manager in hierarchy
     */
    private User findHigherLevelManager(User currentManager) {
        Role currentRole = currentManager.getRole();
        
        switch (currentRole) {
            case ROLE_BLOCK_MANAGER:
                // Escalate to district manager
                return findUserWithRole(Role.ROLE_DISTRICT_MANAGER);
            case ROLE_DISTRICT_MANAGER:
                // Escalate to sambhag manager
                return findUserWithRole(Role.ROLE_SAMBHAG_MANAGER);
            case ROLE_SAMBHAG_MANAGER:
                // Escalate to admin
                return findUserWithRole(Role.ROLE_ADMIN);
            default:
                return null;
        }
    }
    
    /**
     * Find user with specific role (simplified)
     */
    private User findUserWithRole(Role role) {
        return userRepository.findByRole(role).stream().findFirst().orElse(null);
    }
    
    /**
     * Escalate priority level
     */
    private QueryPriority escalatePriority(QueryPriority currentPriority) {
        switch (currentPriority) {
            case LOW: return QueryPriority.MEDIUM;
            case MEDIUM: return QueryPriority.HIGH;
            case HIGH: 
            case URGENT: return QueryPriority.URGENT;
            default: return QueryPriority.HIGH;
        }
    }
    
    /**
     * Check if user can assign query
     */
    private boolean canAssignQuery(User assignedBy, ManagerQuery query, User targetManager) {
        // Admin can assign anything
        if (assignedBy.getRole() == Role.ROLE_ADMIN) {
            return true;
        }
        
        // Can assign if you're higher in hierarchy than target
        return isHigherInHierarchy(assignedBy.getRole(), targetManager.getRole());
    }
    
    /**
     * Check if user can update query
     */
    private boolean canUpdateQuery(User user, ManagerQuery query) {
        // Admin can update anything
        if (user.getRole() == Role.ROLE_ADMIN) {
            return true;
        }
        
        // Creator can update their own query
        if (query.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }
        
        // Assigned manager can update
        if (query.getAssignedTo() != null && query.getAssignedTo().getId().equals(user.getId())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check hierarchy level
     */
    private boolean isHigherInHierarchy(Role role1, Role role2) {
        int level1 = getRoleLevel(role1);
        int level2 = getRoleLevel(role2);
        return level1 > level2;
    }
    
    /**
     * Get numeric role level for hierarchy comparison
     */
    private int getRoleLevel(Role role) {
        switch (role) {
            case ROLE_ADMIN: return 4;
            case ROLE_SAMBHAG_MANAGER: return 3;
            case ROLE_DISTRICT_MANAGER: return 2;
            case ROLE_BLOCK_MANAGER: return 1;
            default: return 0;
        }
    }
    
    /**
     * Map entity to response DTO
     */
    private ManagerQueryResponse mapToResponse(ManagerQuery query) {
        ManagerQueryResponse.ManagerQueryResponseBuilder responseBuilder = ManagerQueryResponse.builder()
            .id(query.getId())
            .title(query.getTitle())
            .description(query.getDescription())
            .priority(query.getPriority())
            .status(query.getStatus())
            .resolution(query.getResolution())
            .createdAt(query.getCreatedAt())
            .assignedAt(query.getAssignedAt())
            .resolvedAt(query.getResolvedAt())
            .updatedAt(query.getUpdatedAt());
        
        // Creator details
        if (query.getCreatedBy() != null) {
            responseBuilder.createdById(query.getCreatedBy().getId())
                          .createdByName(query.getCreatedBy().getName())
                          .createdByEmail(query.getCreatedBy().getEmail());
        }
        
        // Assigned to details
        if (query.getAssignedTo() != null) {
            responseBuilder.assignedToId(query.getAssignedTo().getId())
                          .assignedToName(query.getAssignedTo().getName())
                          .assignedToEmail(query.getAssignedTo().getEmail());
        }
        
        // Resolved by details
        if (query.getResolvedBy() != null) {
            responseBuilder.resolvedById(query.getResolvedBy().getId())
                          .resolvedByName(query.getResolvedBy().getName());
        }
        
        // Location details
        if (query.getRelatedSambhag() != null) {
            responseBuilder.relatedSambhagId(query.getRelatedSambhag().getId())
                          .relatedSambhagName(query.getRelatedSambhag().getName());
        }
        
        if (query.getRelatedDistrict() != null) {
            responseBuilder.relatedDistrictId(query.getRelatedDistrict().getId())
                          .relatedDistrictName(query.getRelatedDistrict().getName());
        }
        
        if (query.getRelatedBlock() != null) {
            responseBuilder.relatedBlockId(query.getRelatedBlock().getId())
                          .relatedBlockName(query.getRelatedBlock().getName());
        }
        
        // Related user details
        if (query.getRelatedUser() != null) {
            responseBuilder.relatedUserId(query.getRelatedUser().getId())
                          .relatedUserName(query.getRelatedUser().getName());
        }
        
        return responseBuilder.build();
    }
    
    /**
     * DTO for query statistics
     */
    public static class ManagerQueryStats {
        private int pendingCount;
        private int inProgressCount;
        private int resolvedCount;
        private int overdueCount;
        private int totalAssigned;
        
        // Constructors, getters, setters
        public static ManagerQueryStatsBuilder builder() {
            return new ManagerQueryStatsBuilder();
        }
        
        // Builder pattern implementation
        public static class ManagerQueryStatsBuilder {
            private int pendingCount;
            private int inProgressCount;
            private int resolvedCount;
            private int overdueCount;
            private int totalAssigned;
            
            public ManagerQueryStatsBuilder pendingCount(int pendingCount) {
                this.pendingCount = pendingCount;
                return this;
            }
            
            public ManagerQueryStatsBuilder inProgressCount(int inProgressCount) {
                this.inProgressCount = inProgressCount;
                return this;
            }
            
            public ManagerQueryStatsBuilder resolvedCount(int resolvedCount) {
                this.resolvedCount = resolvedCount;
                return this;
            }
            
            public ManagerQueryStatsBuilder overdueCount(int overdueCount) {
                this.overdueCount = overdueCount;
                return this;
            }
            
            public ManagerQueryStatsBuilder totalAssigned(int totalAssigned) {
                this.totalAssigned = totalAssigned;
                return this;
            }
            
            public ManagerQueryStats build() {
                ManagerQueryStats stats = new ManagerQueryStats();
                stats.pendingCount = this.pendingCount;
                stats.inProgressCount = this.inProgressCount;
                stats.resolvedCount = this.resolvedCount;
                stats.overdueCount = this.overdueCount;
                stats.totalAssigned = this.totalAssigned;
                return stats;
            }
        }
        
        // Getters
        public int getPendingCount() { return pendingCount; }
        public int getInProgressCount() { return inProgressCount; }
        public int getResolvedCount() { return resolvedCount; }
        public int getOverdueCount() { return overdueCount; }
        public int getTotalAssigned() { return totalAssigned; }
    }
}