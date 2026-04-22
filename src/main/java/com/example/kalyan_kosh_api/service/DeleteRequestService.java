package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.DeleteRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DeleteRequestService {

    private final DeleteRequestRepository deleteRequestRepository;

    public DeleteRequestService(DeleteRequestRepository deleteRequestRepository) {
        this.deleteRequestRepository = deleteRequestRepository;
    }

    @Transactional
    public DeleteRequest createDeleteRequest(
            DeleteEntityType entityType,
            String entityId,
            User requestedBy,
            String reason,
            String requestedFromDashboard
    ) {
        DeleteRequest request = new DeleteRequest();
        request.setEntityType(entityType);
        request.setEntityId(entityId);
        request.setRequestedBy(requestedBy);
        request.setRequestedByRole(requestedBy.getRole());
        request.setRequestedFromDashboard(requestedFromDashboard);
        request.setReason(reason);
        request.setStatus(DeleteRequestStatus.PENDING);
        request.setApprovalLevel("SUPERADMIN");
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        return deleteRequestRepository.save(request);
    }

    @Transactional
    public DeleteRequest approveRequest(Long requestId, User approvedBy) {
        DeleteRequest request = getById(requestId);
        request.setStatus(DeleteRequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(Instant.now());
        request.setUpdatedAt(Instant.now());
        return deleteRequestRepository.save(request);
    }

    @Transactional
    public DeleteRequest rejectRequest(Long requestId, User rejectedBy, String rejectionReason) {
        DeleteRequest request = getById(requestId);
        request.setStatus(DeleteRequestStatus.REJECTED);
        request.setRejectedBy(rejectedBy);
        request.setRejectedAt(Instant.now());
        request.setRejectionReason(rejectionReason);
        request.setUpdatedAt(Instant.now());
        return deleteRequestRepository.save(request);
    }

    @Transactional
    public DeleteRequest markExecuted(Long requestId) {
        DeleteRequest request = getById(requestId);
        request.setStatus(DeleteRequestStatus.EXECUTED);
        request.setExecutedAt(Instant.now());
        request.setUpdatedAt(Instant.now());
        return deleteRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public DeleteRequest getById(Long requestId) {
        return deleteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Delete request not found with id: " + requestId));
    }

    @Transactional(readOnly = true)
    public List<DeleteRequest> getPendingRequests() {
        return deleteRequestRepository.findByStatusOrderByCreatedAtDesc(DeleteRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<DeleteRequest> getRequestsByUser(String userId) {
        return deleteRequestRepository.findByRequestedBy_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<DeleteRequest> getEntityRequests(DeleteEntityType entityType, String entityId) {
        return deleteRequestRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
}