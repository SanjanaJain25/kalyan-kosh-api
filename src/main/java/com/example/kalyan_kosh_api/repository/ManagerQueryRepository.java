package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.ManagerQuery;
import com.example.kalyan_kosh_api.entity.QueryStatus;
import com.example.kalyan_kosh_api.entity.QueryPriority;
import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ManagerQueryRepository extends JpaRepository<ManagerQuery, Long> {
    
    // Find queries created by specific manager
    Page<ManagerQuery> findByCreatedByOrderByCreatedAtDesc(User createdBy, Pageable pageable);
    
    // Find queries assigned to specific manager
    Page<ManagerQuery> findByAssignedToOrderByCreatedAtDesc(User assignedTo, Pageable pageable);
    
    // Find queries by status
    Page<ManagerQuery> findByStatusOrderByCreatedAtDesc(QueryStatus status, Pageable pageable);
    
    // Find queries by priority
    Page<ManagerQuery> findByPriorityOrderByCreatedAtDesc(QueryPriority priority, Pageable pageable);
    
    // Find queries by status and assigned user
    Page<ManagerQuery> findByStatusAndAssignedToOrderByCreatedAtDesc(
        QueryStatus status, User assignedTo, Pageable pageable
    );
    
    // Find queries related to specific location areas for sambhag manager
    @Query("SELECT mq FROM ManagerQuery mq WHERE " +
           "mq.relatedSambhag.id IN :sambhagIds " +
           "ORDER BY mq.createdAt DESC")
    Page<ManagerQuery> findBySambhagScope(@Param("sambhagIds") List<Long> sambhagIds, Pageable pageable);
    
    // Find queries related to specific districts for district manager
    @Query("SELECT mq FROM ManagerQuery mq WHERE " +
           "mq.relatedDistrict.id IN :districtIds " +
           "ORDER BY mq.createdAt DESC")
    Page<ManagerQuery> findByDistrictScope(@Param("districtIds") List<Long> districtIds, Pageable pageable);
    
    // Find queries related to specific blocks for block manager
    @Query("SELECT mq FROM ManagerQuery mq WHERE " +
           "mq.relatedBlock.id IN :blockIds " +
           "ORDER BY mq.createdAt DESC")
    Page<ManagerQuery> findByBlockScope(@Param("blockIds") List<Long> blockIds, Pageable pageable);
    
    // Find pending queries that need attention
    @Query("SELECT mq FROM ManagerQuery mq WHERE " +
           "mq.status = 'PENDING' AND " +
           "(mq.assignedTo IS NULL OR mq.assignedTo = :manager) " +
           "ORDER BY mq.priority DESC, mq.createdAt ASC")
    Page<ManagerQuery> findPendingQueriesForManager(@Param("manager") User manager, Pageable pageable);
    
    // Count queries by status for dashboard
    @Query("SELECT COUNT(mq) FROM ManagerQuery mq WHERE mq.status = :status AND mq.assignedTo = :manager")
    Long countByStatusAndAssignedTo(@Param("status") QueryStatus status, @Param("manager") User manager);
    
    // Find overdue queries (older than specified hours)
    @Query("SELECT mq FROM ManagerQuery mq WHERE " +
           "mq.status IN ('PENDING', 'IN_PROGRESS') AND " +
           "mq.createdAt < :cutoffTime " +
           "ORDER BY mq.createdAt ASC")
    List<ManagerQuery> findOverdueQueries(@Param("cutoffTime") Instant cutoffTime);
    
    // Complex query for manager dashboard with multiple filters
    @Query("SELECT mq FROM ManagerQuery mq WHERE " +
           "(:createdBy IS NULL OR mq.createdBy = :createdBy) AND " +
           "(:assignedTo IS NULL OR mq.assignedTo = :assignedTo) AND " +
           "(:status IS NULL OR mq.status = :status) AND " +
           "(:priority IS NULL OR mq.priority = :priority) AND " +
           "(:sambhagId IS NULL OR mq.relatedSambhag.id = :sambhagId) AND " +
           "(:districtId IS NULL OR mq.relatedDistrict.id = :districtId) AND " +
           "(:blockId IS NULL OR mq.relatedBlock.id = :blockId) " +
           "ORDER BY mq.priority DESC, mq.createdAt DESC")
    Page<ManagerQuery> findWithFilters(
        @Param("createdBy") User createdBy,
        @Param("assignedTo") User assignedTo,
        @Param("status") QueryStatus status,
        @Param("priority") QueryPriority priority,
        @Param("sambhagId") Long sambhagId,
        @Param("districtId") Long districtId,
        @Param("blockId") Long blockId,
        Pageable pageable
    );
}