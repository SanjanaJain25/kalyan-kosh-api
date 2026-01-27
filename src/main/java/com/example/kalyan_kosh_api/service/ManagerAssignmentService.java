package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.*;
import com.example.kalyan_kosh_api.dto.manager.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Manager Assignments
 * Handles assignment creation, updates, and validation
 */
@Service
@Transactional
public class ManagerAssignmentService {
    
    @Autowired
    private ManagerAssignmentRepository managerAssignmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SambhagRepository sambhagRepository;
    
    @Autowired
    private DistrictRepository districtRepository;
    
    @Autowired
    private BlockRepository blockRepository;
    
    /**
     * Create new manager assignment
     */
    public ManagerAssignmentResponse createAssignment(CreateManagerAssignmentRequest request, User assignedBy) {
        // Validate request
        if (!request.isValidAssignment()) {
            throw new IllegalArgumentException("Invalid location assignment for manager level: " + 
                request.getManagerLevel() + ". Expected: " + request.getExpectedLocationLevel());
        }
        
        // Get manager user
        User manager = userRepository.findById(request.getManagerId())
            .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + request.getManagerId()));
        
        // Verify manager has appropriate role
        if (!isValidManagerRole(manager, request.getManagerLevel())) {
            throw new IllegalArgumentException("User does not have appropriate role for " + request.getManagerLevel() + " level");
        }
        
        // Check if assignment already exists
        UUID sambhagUuid = request.getSambhagId() != null ? UUID.fromString(request.getSambhagId()) : null;
        UUID districtUuid = request.getDistrictId() != null ? UUID.fromString(request.getDistrictId()) : null;
        UUID blockUuid = request.getBlockId() != null ? UUID.fromString(request.getBlockId()) : null;

        if (managerAssignmentRepository.existsActiveAssignment(
            manager, sambhagUuid, districtUuid, blockUuid)) {
            throw new IllegalArgumentException("Manager is already assigned to this location");
        }
        
        // Build assignment entity
        ManagerAssignment.ManagerAssignmentBuilder assignmentBuilder = ManagerAssignment.builder()
            .manager(manager)
            .managerLevel(request.getManagerLevel())
            .assignedBy(assignedBy)
            .assignedAt(Instant.now())
            .notes(request.getNotes())
            .isActive(true);
        
        // Set location based on assignment type
        if (request.getSambhagId() != null) {
            Sambhag sambhag = sambhagRepository.findById(UUID.fromString(request.getSambhagId()))
                .orElseThrow(() -> new IllegalArgumentException("Sambhag not found"));
            assignmentBuilder.sambhag(sambhag);
        }
        
        if (request.getDistrictId() != null) {
            District district = districtRepository.findById(UUID.fromString(request.getDistrictId()))
                .orElseThrow(() -> new IllegalArgumentException("District not found"));
            assignmentBuilder.district(district);
        }
        
        if (request.getBlockId() != null) {
            Block block = blockRepository.findById(UUID.fromString(request.getBlockId()))
                .orElseThrow(() -> new IllegalArgumentException("Block not found"));
            assignmentBuilder.block(block);
        }
        
        ManagerAssignment assignment = assignmentBuilder.build();
        ManagerAssignment savedAssignment = managerAssignmentRepository.save(assignment);
        
        return mapToResponse(savedAssignment);
    }
    
    /**
     * Get all assignments for a manager
     */
    @Transactional(readOnly = true)
    public List<ManagerAssignmentResponse> getManagerAssignments(String managerId) {
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
        
        List<ManagerAssignment> assignments = managerAssignmentRepository.findByManagerAndIsActiveTrue(manager);
        
        return assignments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Deactivate manager assignment
     */
    public void removeAssignment(Long assignmentId, User removedBy) {
        ManagerAssignment assignment = managerAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        assignment.setActive(false);
//        assignment.u(Instant.now());
        
        managerAssignmentRepository.save(assignment);
    }
    
    /**
     * Update assignment notes
     */
    public ManagerAssignmentResponse updateAssignmentNotes(Long assignmentId, String notes) {
        ManagerAssignment assignment = managerAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        assignment.setNotes(notes);
//        assignment.setUpdatedAt(Instant.now());
        
        ManagerAssignment savedAssignment = managerAssignmentRepository.save(assignment);
        return mapToResponse(savedAssignment);
    }
    
    /**
     * Get assignments by location
     */
    @Transactional(readOnly = true)
    public List<ManagerAssignmentResponse> getAssignmentsByLocation(UUID sambhagId, UUID districtId, UUID blockId) {
        List<ManagerAssignment> assignments;
        
        if (blockId != null) {
            assignments = managerAssignmentRepository.findByBlockId(blockId);
        } else if (districtId != null) {
            assignments = managerAssignmentRepository.findByDistrictId(districtId);
        } else if (sambhagId != null) {
            assignments = managerAssignmentRepository.findBySambhagId(sambhagId);
        } else {
            throw new IllegalArgumentException("At least one location ID must be provided");
        }
        
        return assignments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Helper method to validate manager role
     */
    private boolean isValidManagerRole(User manager, ManagerLevel level) {
        Role userRole = manager.getRole();
        
        switch (level) {
            case SAMBHAG:
                return userRole == Role.ROLE_SAMBHAG_MANAGER || userRole == Role.ROLE_ADMIN;
            case DISTRICT:
                return userRole == Role.ROLE_DISTRICT_MANAGER || userRole == Role.ROLE_ADMIN;
            case BLOCK:
                return userRole == Role.ROLE_BLOCK_MANAGER || userRole == Role.ROLE_ADMIN;
            default:
                return false;
        }
    }
    
    /**
     * Map entity to response DTO
     */
    private ManagerAssignmentResponse mapToResponse(ManagerAssignment assignment) {
        ManagerAssignmentResponse.ManagerAssignmentResponseBuilder responseBuilder = 
            ManagerAssignmentResponse.builder()
                .id(assignment.getId())
                .managerId(assignment.getManager().getId())
                .managerName(assignment.getManager().getName())
                .managerEmail(assignment.getManager().getEmail())
                .managerLevel(assignment.getManagerLevel())
//                .isActive(assignment.getIsActive())
                .assignedAt(assignment.getAssignedAt())
                .notes(assignment.getNotes());
        
        if (assignment.getAssignedBy() != null) {
            responseBuilder.assignedByName(assignment.getAssignedBy().getName());
        }
        
        // Set location details
        if (assignment.getSambhag() != null) {
            responseBuilder.sambhagId(String.valueOf(assignment.getSambhag().getId()))
                          .sambhagName(assignment.getSambhag().getName());
        }
        
        if (assignment.getDistrict() != null) {
            responseBuilder.districtId(String.valueOf(assignment.getDistrict().getId()))
                          .districtName(assignment.getDistrict().getName());
        }
        
        if (assignment.getBlock() != null) {
            responseBuilder.blockId(String.valueOf(assignment.getBlock().getId()))
                          .blockName(assignment.getBlock().getName());
        }
        
        return responseBuilder.build();
    }
}