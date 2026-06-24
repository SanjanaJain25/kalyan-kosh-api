package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.ManagerDeletePermissionResponse;
import com.example.kalyan_kosh_api.entity.ManagerDeletePermission;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.ManagerDeletePermissionRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ManagerDeletePermissionService {

    private final ManagerDeletePermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public ManagerDeletePermissionService(
            ManagerDeletePermissionRepository permissionRepository,
            UserRepository userRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public boolean canDeleteUsers(User currentUser) {
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }

        if (
                currentUser.getRole() == Role.ROLE_SUPERADMIN
                        || currentUser.getRole() == Role.ROLE_ADMIN
        ) {
            return true;
        }

        return permissionRepository.existsByUser_IdAndEnabledTrue(currentUser.getId());
    }

    public Page<ManagerDeletePermissionResponse> listPermissions(Pageable pageable) {
        return permissionRepository.findAllWithUsers(pageable)
                .map(this::toResponse);
    }

    public ManagerDeletePermissionResponse checkPermission(String userId) {
        User user = getUserOrThrow(userId);

        ManagerDeletePermission permission = permissionRepository.findByUser_Id(userId)
                .orElse(null);

        if (permission == null) {
            return ManagerDeletePermissionResponse.builder()
                    .id(null)
                    .userId(user.getId())
                    .userName(buildFullName(user))
                    .userRole(user.getRole() != null ? user.getRole().name() : null)
                    .userStatus(user.getStatus() != null ? user.getStatus().name() : null)
                    .mobileNumber(user.getMobileNumber())
                    .enabled(false)
                    .build();
        }

        return toResponse(permission);
    }

    @Transactional
    public ManagerDeletePermissionResponse grantPermission(
            String userId,
            String remarks,
            User actingUser
    ) {
        User targetUser = getUserOrThrow(userId);
        validateManagerRole(targetUser);

        ManagerDeletePermission permission = permissionRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    ManagerDeletePermission p = new ManagerDeletePermission();
                    p.setUser(targetUser);
                    return p;
                });

        permission.setEnabled(true);
        permission.setGrantedBy(actingUser);
        permission.setGrantedAt(Instant.now());
        permission.setRevokedBy(null);
        permission.setRevokedAt(null);
        permission.setRemarks(cleanText(remarks));

        return toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public ManagerDeletePermissionResponse revokePermission(
            String userId,
            String remarks,
            User actingUser
    ) {
        User targetUser = getUserOrThrow(userId);

        ManagerDeletePermission permission = permissionRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    ManagerDeletePermission p = new ManagerDeletePermission();
                    p.setUser(targetUser);
                    return p;
                });

        permission.setEnabled(false);
        permission.setRevokedBy(actingUser);
        permission.setRevokedAt(Instant.now());
        permission.setRemarks(cleanText(remarks));

        return toResponse(permissionRepository.save(permission));
    }

    private User getUserOrThrow(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository.findById(userId.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    private void validateManagerRole(User user) {
        Role role = user.getRole();

        if (
                role != Role.ROLE_SAMBHAG_MANAGER
                        && role != Role.ROLE_DISTRICT_MANAGER
                        && role != Role.ROLE_BLOCK_MANAGER
        ) {
            throw new IllegalArgumentException("Delete permission can be granted only to manager users.");
        }
    }

    private ManagerDeletePermissionResponse toResponse(ManagerDeletePermission permission) {
        User targetUser = permission.getUser();
        User grantedBy = permission.getGrantedBy();
        User revokedBy = permission.getRevokedBy();

        return ManagerDeletePermissionResponse.builder()
                .id(permission.getId())
                .userId(targetUser != null ? targetUser.getId() : null)
                .userName(buildFullName(targetUser))
                .userRole(targetUser != null && targetUser.getRole() != null ? targetUser.getRole().name() : null)
                .userStatus(targetUser != null && targetUser.getStatus() != null ? targetUser.getStatus().name() : null)
                .mobileNumber(targetUser != null ? targetUser.getMobileNumber() : null)
                .enabled(permission.isEnabled())
                .grantedById(grantedBy != null ? grantedBy.getId() : null)
                .grantedByName(buildFullName(grantedBy))
                .grantedAt(permission.getGrantedAt())
                .revokedById(revokedBy != null ? revokedBy.getId() : null)
                .revokedByName(buildFullName(revokedBy))
                .revokedAt(permission.getRevokedAt())
                .remarks(permission.getRemarks())
                .build();
    }

    private String buildFullName(User user) {
        if (user == null) {
            return null;
        }

        String first = user.getName() == null ? "" : user.getName().trim();
        String last = user.getSurname() == null ? "" : user.getSurname().trim();

        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? user.getId() : fullName;
    }

    private String cleanText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}