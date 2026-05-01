package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.DeleteEntityType;
import com.example.kalyan_kosh_api.entity.DeleteRequest;
import com.example.kalyan_kosh_api.entity.DeleteRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DeleteRequestRepository extends JpaRepository<DeleteRequest, Long> {

    List<DeleteRequest> findByStatusOrderByCreatedAtDesc(DeleteRequestStatus status);

    List<DeleteRequest> findByRequestedBy_IdOrderByCreatedAtDesc(String requestedBy);

    List<DeleteRequest> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(DeleteEntityType entityType, String entityId);

    Optional<DeleteRequest> findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc(DeleteEntityType entityType, String entityId);
    @Modifying
@Query("""
    DELETE FROM DeleteRequest dr
    WHERE 
        (dr.entityType = :entityType AND dr.entityId = :userId)
        OR dr.requestedBy.id = :userId
        OR dr.approvedBy.id = :userId
        OR dr.rejectedBy.id = :userId
        OR dr.restoreRequestedBy.id = :userId
        OR dr.restoreApprovedBy.id = :userId
""")
void deleteUserRelatedRequests(
        @Param("entityType") DeleteEntityType entityType,
        @Param("userId") String userId
);
}