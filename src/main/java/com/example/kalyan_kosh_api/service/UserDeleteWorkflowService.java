package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.List;

@Service
public class UserDeleteWorkflowService {

    private final UserRepository userRepository;
    private final DeleteRequestService deleteRequestService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public UserDeleteWorkflowService(
            UserRepository userRepository,
            DeleteRequestService deleteRequestService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.deleteRequestService = deleteRequestService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
public User softDeleteUser(
        String userId,
        User actingUser,
        String reason,
        String requestedFromDashboard,
        HttpServletRequest httpRequest
) {
    User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    if (targetUser.getRole() == Role.ROLE_SUPERADMIN) {
        throw new IllegalArgumentException("Super Admin cannot be deleted.");
    }

    if (targetUser.getStatus() == UserStatus.DELETED) {
        throw new IllegalArgumentException("User is already in trash.");
    }

    // only create request here, do NOT soft delete now
    deleteRequestService.createDeleteRequest(
            DeleteEntityType.USER,
            targetUser.getId(),
            actingUser,
            reason,
            requestedFromDashboard
    );

    auditLogService.saveLog(
            DeleteEntityType.USER,
            targetUser.getId(),
            AuditActionType.DELETE_REQUEST_CREATED,
            null,
            null,
            actingUser,
            reason != null ? reason : "Delete request created",
            getIpAddress(httpRequest)
    );

    return targetUser;
}

    @Transactional
    public User restoreUser(
            String userId,
            User actingUser,
            HttpServletRequest httpRequest
    ) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

if (targetUser.getStatus() != UserStatus.DELETED) {
    throw new IllegalArgumentException("Only deleted users can be restored.");
}
        String oldJson = toJsonQuietly(targetUser);

        targetUser.setStatus(UserStatus.ACTIVE);
        targetUser.setDeletedAt(null);
        targetUser.setDeletedBy(null);
        targetUser.setDeleteReason(null);

        User saved = userRepository.save(targetUser);

        auditLogService.saveLog(
                DeleteEntityType.USER,
                saved.getId(),
                AuditActionType.RESTORE,
                oldJson,
                toJsonQuietly(saved),
                actingUser,
                "User restored from trash",
                getIpAddress(httpRequest)
        );

        return saved;
    }


@Transactional
public DeleteRequest approveDeleteRequest(
        Long deleteRequestId,
        User actingUser,
        HttpServletRequest httpRequest
) {
    if (actingUser.getRole() != Role.ROLE_ADMIN && actingUser.getRole() != Role.ROLE_SUPERADMIN) {
        throw new IllegalArgumentException("Only Admin or Super Admin can approve delete request.");
    }

    DeleteRequest request = deleteRequestService.approveRequest(deleteRequestId, actingUser);

    if (request.getEntityType() == DeleteEntityType.USER) {
        User targetUser = userRepository.findById(request.getEntityId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getEntityId()));

        if (targetUser.getStatus() != UserStatus.DELETED) {
            String oldJson = toJsonQuietly(targetUser);

            targetUser.setStatus(UserStatus.DELETED);
            targetUser.setDeletedAt(Instant.now());
            targetUser.setDeletedBy(actingUser);
            targetUser.setDeleteReason(request.getReason());
            targetUser.setUpdatedAt(Instant.now());

            User saved = userRepository.save(targetUser);

            auditLogService.saveLog(
                    DeleteEntityType.USER,
                    saved.getId(),
                    AuditActionType.SOFT_DELETE,
                    oldJson,
                    toJsonQuietly(saved),
                    actingUser,
                    request.getReason() != null ? request.getReason() : "User moved to trash after approval",
                    getIpAddress(httpRequest)
            );
        }
    }

    auditLogService.saveLog(
            request.getEntityType(),
            request.getEntityId(),
            AuditActionType.DELETE_REQUEST_APPROVED,
            null,
            null,
            actingUser,
            "Delete request approved and user moved to trash",
            getIpAddress(httpRequest)
    );

    return request;
}
@Transactional
public void permanentlyDeleteUserFromTrash(
        String userId,
        User actingUser,
        HttpServletRequest httpRequest
) {
    if (actingUser.getRole() != Role.ROLE_SUPERADMIN && actingUser.getRole() != Role.ROLE_ADMIN) {
        throw new IllegalArgumentException("Only Admin or Super Admin can permanently delete users from trash.");
    }

    User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    if (targetUser.getStatus() != UserStatus.DELETED) {
        throw new IllegalArgumentException("Only deleted users in trash can be permanently deleted.");
    }

    String oldJson = toJsonQuietly(targetUser);

    userRepository.delete(targetUser);

    auditLogService.saveLog(
            DeleteEntityType.USER,
            userId,
            AuditActionType.HARD_DELETE,
            oldJson,
            null,
            actingUser,
            "User permanently deleted from trash",
            getIpAddress(httpRequest)
    );
}
   @Transactional
public DeleteRequest rejectDeleteRequest(
        Long deleteRequestId,
        User actingUser,
        String rejectionReason,
        HttpServletRequest httpRequest
) {
    if (actingUser.getRole() != Role.ROLE_SUPERADMIN && actingUser.getRole() != Role.ROLE_ADMIN) {
        throw new IllegalArgumentException("Only Admin or Super Admin can reject permanent delete.");
    }

    DeleteRequest request = deleteRequestService.rejectRequest(deleteRequestId, actingUser, rejectionReason);

    if (request.getEntityType() == DeleteEntityType.USER) {
        User targetUser = userRepository.findById(request.getEntityId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getEntityId()));

        String oldJson = toJsonQuietly(targetUser);

        if (targetUser.getStatus() == UserStatus.DELETED) {
            targetUser.setStatus(UserStatus.ACTIVE);
            targetUser.setDeletedAt(null);
            targetUser.setDeletedBy(null);
            targetUser.setDeleteReason(null);

            User restored = userRepository.save(targetUser);

            auditLogService.saveLog(
                    DeleteEntityType.USER,
                    restored.getId(),
                    AuditActionType.RESTORE,
                    oldJson,
                    toJsonQuietly(restored),
                    actingUser,
                    "User restored because delete request was rejected",
                    getIpAddress(httpRequest)
            );
        }
    }

    auditLogService.saveLog(
            request.getEntityType(),
            request.getEntityId(),
            AuditActionType.DELETE_REQUEST_REJECTED,
            null,
            null,
            actingUser,
            rejectionReason != null ? rejectionReason : "Delete request rejected",
            getIpAddress(httpRequest)
    );

    return request;
}
@Transactional
public int restoreAllDeletedUsers(User actingUser, HttpServletRequest httpRequest) {
    List<User> deletedUsers = userRepository.findDeletedUsers(UserStatus.DELETED);

    for (User user : deletedUsers) {
        user.setStatus(UserStatus.ACTIVE);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setDeleteReason(null);
        user.setUpdatedAt(Instant.now());

        auditLogService.saveLog(
                DeleteEntityType.USER,
                user.getId(),
                AuditActionType.RESTORE,
                null,
                null,
                actingUser,
                "User restored from restore all",
                getIpAddress(httpRequest)
        );
    }

    userRepository.saveAll(deletedUsers);
    return deletedUsers.size();
}

@Transactional
public int permanentlyDeleteAllUsersFromTrash(User actingUser, HttpServletRequest httpRequest) {
    List<User> deletedUsers = userRepository.findDeletedUsers(UserStatus.DELETED);
    int count = deletedUsers.size();

    for (User user : deletedUsers) {
        auditLogService.saveLog(
                DeleteEntityType.USER,
                user.getId(),
                AuditActionType.HARD_DELETE,
                null,
                null,
                actingUser,
                "User permanently deleted from clear all",
                getIpAddress(httpRequest)
        );
    }

    userRepository.deleteAll(deletedUsers);
    return count;
}
@Transactional(readOnly = true)
public List<Map<String, Object>> getDeletedUsersForTrash() {
    List<User> users = userRepository.findDeletedUsers(UserStatus.DELETED);

    return users.stream().map(u -> {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("surname", u.getSurname());
        map.put("email", u.getEmail());
        map.put("mobileNumber", u.getMobileNumber());
        map.put("role", u.getRole());
        map.put("status", u.getStatus());
        map.put("deletedAt", u.getDeletedAt());
        map.put("deleteReason", u.getDeleteReason());

        if (u.getDeletedBy() != null) {
            map.put("deletedById", u.getDeletedBy().getId());
            map.put("deletedByName", u.getDeletedBy().getName());
        } else {
            map.put("deletedById", null);
            map.put("deletedByName", null);
        }

        return map;
    }).toList();
}

 @Transactional(readOnly = true)
public List<User> getDeletedUsers() {
    return userRepository.findDeletedUsers(UserStatus.DELETED);
}

    private String toJsonQuietly(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}