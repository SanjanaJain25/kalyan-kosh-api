package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.ManagerAssignment;
import com.example.kalyan_kosh_api.entity.ManagerLevel;
import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerAssignmentRepository extends JpaRepository<ManagerAssignment, Long> {
    
    // Find all assignments for a manager
    List<ManagerAssignment> findByManagerAndIsActiveTrue(User manager);
    
    // Find assignments by manager level
    List<ManagerAssignment> findByManagerLevelAndIsActiveTrue(ManagerLevel level);
    
    // Find who manages a specific sambhag
    @Query("SELECT ma FROM ManagerAssignment ma WHERE ma.sambhag.id = :sambhagId AND ma.isActive = true")
    List<ManagerAssignment> findBySambhagId(@Param("sambhagId") UUID sambhagId);

    // Find who manages a specific district
    @Query("SELECT ma FROM ManagerAssignment ma WHERE ma.district.id = :districtId AND ma.isActive = true")
    List<ManagerAssignment> findByDistrictId(@Param("districtId") UUID districtId);

    // Find who manages a specific block
    @Query("SELECT ma FROM ManagerAssignment ma WHERE ma.block.id = :blockId AND ma.isActive = true")
    List<ManagerAssignment> findByBlockId(@Param("blockId") UUID blockId);

    // Check if a user has assignment for specific sambhag
    @Query("SELECT COUNT(ma) > 0 FROM ManagerAssignment ma WHERE ma.manager = :manager AND ma.sambhag.id = :sambhagId AND ma.isActive = true")
    boolean hasAssignmentForSambhag(@Param("manager") User manager, @Param("sambhagId") UUID sambhagId);

    // Check if a user has assignment for specific district  
    @Query("SELECT COUNT(ma) > 0 FROM ManagerAssignment ma WHERE ma.manager = :manager AND ma.district.id = :districtId AND ma.isActive = true")
    boolean hasAssignmentForDistrict(@Param("manager") User manager, @Param("districtId") UUID districtId);

    // Check if a user has assignment for specific block
    @Query("SELECT COUNT(ma) > 0 FROM ManagerAssignment ma WHERE ma.manager = :manager AND ma.block.id = :blockId AND ma.isActive = true")
    boolean hasAssignmentForBlock(@Param("manager") User manager, @Param("blockId") UUID blockId);

    // Find all sambhag IDs managed by a user
    @Query("SELECT DISTINCT ma.sambhag.id FROM ManagerAssignment ma WHERE ma.manager = :manager AND ma.sambhag IS NOT NULL AND ma.isActive = true")
    List<UUID> findSambhagIdsByManager(@Param("manager") User manager);

    // Find all district IDs managed by a user
    @Query("SELECT DISTINCT ma.district.id FROM ManagerAssignment ma WHERE ma.manager = :manager AND ma.district IS NOT NULL AND ma.isActive = true")
    List<UUID> findDistrictIdsByManager(@Param("manager") User manager);

    // Find all block IDs managed by a user
    @Query("SELECT DISTINCT ma.block.id FROM ManagerAssignment ma WHERE ma.manager = :manager AND ma.block IS NOT NULL AND ma.isActive = true")
    List<UUID> findBlockIdsByManager(@Param("manager") User manager);

    // Get all managers for a location with pagination
    @Query("SELECT ma FROM ManagerAssignment ma WHERE " +
           "(ma.sambhag.id = :sambhagId OR ma.district.id = :districtId OR ma.block.id = :blockId) " +
           "AND ma.isActive = true")
    Page<ManagerAssignment> findByLocationIds(
        @Param("sambhagId") UUID sambhagId,
        @Param("districtId") UUID districtId,
        @Param("blockId") UUID blockId,
        Pageable pageable
    );
    
    // Find specific assignment
    Optional<ManagerAssignment> findByManagerAndSambhagIdAndDistrictIdAndBlockIdAndIsActiveTrue(
        User manager, UUID sambhagId, UUID districtId, UUID blockId
    );
    
    // Check if assignment already exists
    @Query("SELECT COUNT(ma) > 0 FROM ManagerAssignment ma WHERE ma.manager = :manager " +
           "AND (:sambhagId IS NULL OR ma.sambhag.id = :sambhagId) " +
           "AND (:districtId IS NULL OR ma.district.id = :districtId) " +
           "AND (:blockId IS NULL OR ma.block.id = :blockId) " +
           "AND ma.isActive = true")
    boolean existsActiveAssignment(
        @Param("manager") User manager,
        @Param("sambhagId") UUID sambhagId,
        @Param("districtId") UUID districtId,
        @Param("blockId") UUID blockId
    );
    
    // Find all active assignments for a manager by manager ID
    @Query("SELECT ma FROM ManagerAssignment ma WHERE ma.manager.id = :managerId AND ma.isActive = true")
    List<ManagerAssignment> findActiveByManagerId(@Param("managerId") String managerId);
    
    // Count active assignments for a manager
    @Query("SELECT COUNT(ma) FROM ManagerAssignment ma WHERE ma.manager.id = :managerId AND ma.isActive = true")
    long countActiveByManagerId(@Param("managerId") String managerId);
}