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
@Query(value = """
    DELETE FROM delete_requests
    WHERE 
        (entity_type = :entityType AND entity_id = :userId)
        OR requested_by = :userId
        OR approved_by = :userId
        OR rejected_by = :userId
        OR restore_requested_by = :userId
        OR restore_approved_by = :userId
""", nativeQuery = true)
int deleteUserRelatedRequests(
        @Param("entityType") String entityType,
        @Param("userId") String userId
);
}