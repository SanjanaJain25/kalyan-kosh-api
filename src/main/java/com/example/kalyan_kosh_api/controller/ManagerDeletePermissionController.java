package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.ManagerDeletePermissionRequest;
import com.example.kalyan_kosh_api.dto.ManagerDeletePermissionResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.ManagerDeletePermissionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/manager-delete-permissions")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
@CrossOrigin(origins = "*")
public class ManagerDeletePermissionController {

    private final ManagerDeletePermissionService permissionService;
    private final UserRepository userRepository;

    public ManagerDeletePermissionController(
            ManagerDeletePermissionService permissionService,
            UserRepository userRepository
    ) {
        this.permissionService = permissionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<ManagerDeletePermissionResponse>> listPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                permissionService.listPermissions(PageRequest.of(page, size))
        );
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<ManagerDeletePermissionResponse> checkPermission(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(permissionService.checkPermission(userId));
    }

    @PostMapping("/grant")
    public ResponseEntity<ManagerDeletePermissionResponse> grantPermission(
            @Valid @RequestBody ManagerDeletePermissionRequest request
    ) {
        User actingUser = getCurrentUser();

        return ResponseEntity.ok(
                permissionService.grantPermission(
                        request.getUserId(),
                        request.getRemarks(),
                        actingUser
                )
        );
    }

    @PostMapping("/revoke")
    public ResponseEntity<ManagerDeletePermissionResponse> revokePermission(
            @Valid @RequestBody ManagerDeletePermissionRequest request
    ) {
        User actingUser = getCurrentUser();

        return ResponseEntity.ok(
                permissionService.revokePermission(
                        request.getUserId(),
                        request.getRemarks(),
                        actingUser
                )
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated user");
        }

        return userRepository.findById(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }
}