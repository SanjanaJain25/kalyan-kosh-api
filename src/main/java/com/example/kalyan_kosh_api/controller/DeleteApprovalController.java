package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.DeleteActionRequest;
import com.example.kalyan_kosh_api.dto.DeleteRequestResponse;
import com.example.kalyan_kosh_api.entity.DeleteRequest;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.service.DeleteRequestService;
import com.example.kalyan_kosh_api.service.UserDeleteWorkflowService;
import com.example.kalyan_kosh_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delete-approval")
@CrossOrigin(origins = "*")
public class DeleteApprovalController {

    private final UserDeleteWorkflowService userDeleteWorkflowService;
    private final DeleteRequestService deleteRequestService;
    private final UserService userService;

    public DeleteApprovalController(
            UserDeleteWorkflowService userDeleteWorkflowService,
            DeleteRequestService deleteRequestService,
            UserService userService
    ) {
        this.userDeleteWorkflowService = userDeleteWorkflowService;
        this.deleteRequestService = deleteRequestService;
        this.userService = userService;
    }

    @PostMapping("/users/{userId}/soft-delete")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SAMBHAG_MANAGER')")
    public ResponseEntity<?> softDeleteUser(
            @PathVariable String userId,
            @RequestBody(required = false) DeleteActionRequest request,
            HttpServletRequest httpRequest
    ) {
        User actingUser = getCurrentUser();

        userDeleteWorkflowService.softDeleteUser(
                userId,
                actingUser,
                request != null ? request.getReason() : null,
                request != null ? request.getRequestedFromDashboard() : "UNKNOWN",
                httpRequest
        );

        return ResponseEntity.ok().body(java.util.Map.of(
                "message", "User soft deleted and delete request created successfully."
        ));
    }

    @PostMapping("/users/{userId}/restore")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> restoreUser(
            @PathVariable String userId,
            HttpServletRequest httpRequest
    ) {
        User actingUser = getCurrentUser();

        userDeleteWorkflowService.restoreUser(userId, actingUser, httpRequest);

        return ResponseEntity.ok().body(java.util.Map.of(
                "message", "User restored successfully."
        ));
    }

    @GetMapping("/requests/pending")
  @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<List<DeleteRequestResponse>> getPendingRequests() {
        List<DeleteRequestResponse> result = deleteRequestService.getPendingRequests()
                .stream()
                .map(DeleteRequestResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests/my")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SAMBHAG_MANAGER','DISTRICT_MANAGER','BLOCK_MANAGER')")
    public ResponseEntity<List<DeleteRequestResponse>> getMyRequests() {
        User actingUser = getCurrentUser();

        List<DeleteRequestResponse> result = deleteRequestService.getRequestsByUser(actingUser.getId())
                .stream()
                .map(DeleteRequestResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> approveDeleteRequest(
            @PathVariable Long requestId,
            HttpServletRequest httpRequest
    ) {
        User actingUser = getCurrentUser();

userDeleteWorkflowService.approveDeleteRequest(requestId, actingUser, httpRequest);
        return ResponseEntity.ok().body(java.util.Map.of(
        "message", "Delete request approved successfully. User remains in trash until permanent deletion."
));
    }
@PostMapping("/users/{userId}/permanent-delete")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public ResponseEntity<?> permanentlyDeleteUserFromTrash(
        @PathVariable String userId,
        HttpServletRequest httpRequest
) {
    User actingUser = getCurrentUser();

    userDeleteWorkflowService.permanentlyDeleteUserFromTrash(userId, actingUser, httpRequest);

    return ResponseEntity.ok().body(java.util.Map.of(
            "message", "User permanently deleted from trash successfully."
    ));
}
    @PostMapping("/requests/{requestId}/reject")
  @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<?> rejectDeleteRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) DeleteActionRequest request,
            HttpServletRequest httpRequest
    ) {
        User actingUser = getCurrentUser();

        DeleteRequest deleteRequest = userDeleteWorkflowService.rejectDeleteRequest(
                requestId,
                actingUser,
                request != null ? request.getRejectionReason() : null,
                httpRequest
        );

        return ResponseEntity.ok(DeleteRequestResponse.fromEntity(deleteRequest));
    }

    @PostMapping("/trash/users/restore-all")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public ResponseEntity<?> restoreAllDeletedUsers(HttpServletRequest httpRequest) {
    User actingUser = getCurrentUser();
    int restored = userDeleteWorkflowService.restoreAllDeletedUsers(actingUser, httpRequest);

    return ResponseEntity.ok(java.util.Map.of(
            "success", true,
            "message", restored + " users restored successfully."
    ));
}

@PostMapping("/trash/users/clear-all")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public ResponseEntity<?> clearAllTrash(HttpServletRequest httpRequest) {
    User actingUser = getCurrentUser();
    int deleted = userDeleteWorkflowService.permanentlyDeleteAllUsersFromTrash(actingUser, httpRequest);

    return ResponseEntity.ok(java.util.Map.of(
            "success", true,
            "message", deleted + " users permanently deleted successfully."
    ));
}

   @GetMapping("/trash/users")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public ResponseEntity<?> getDeletedUsers() {
    return ResponseEntity.ok(userDeleteWorkflowService.getDeletedUsersForTrash());
}

    private User getCurrentUser() {
        String currentUserId = userService.getCurrentUserId();
        return userService.findById(currentUserId);
    }
}