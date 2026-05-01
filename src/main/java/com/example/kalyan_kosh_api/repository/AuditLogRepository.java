package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.AuditLog;
import com.example.kalyan_kosh_api.entity.DeleteEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = {"performedBy"})
    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(DeleteEntityType entityType, String entityId);

    @EntityGraph(attributePaths = {"performedBy"})
    List<AuditLog> findByPerformedBy_IdOrderByPerformedAtDesc(String performedBy);

    @Override
    @EntityGraph(attributePaths = {"performedBy"})
    Page<AuditLog> findAll(Pageable pageable);

    @Modifying
@Query("UPDATE AuditLog a SET a.performedBy = NULL WHERE a.performedBy.id = :userId")
void clearPerformedBy(@Param("userId") String userId);
}