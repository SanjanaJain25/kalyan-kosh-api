package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminReportUserRowResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import com.example.kalyan_kosh_api.service.AdminReportService;
import com.example.kalyan_kosh_api.service.ManagerScopeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SAMBHAG_MANAGER','DISTRICT_MANAGER','BLOCK_MANAGER')")
@CrossOrigin(origins = "*")
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final UserRepository userRepository;
    private final ManagerScopeService managerScopeService;

    public AdminReportController(
            AdminReportService adminReportService,
            UserRepository userRepository,
            ManagerScopeService managerScopeService
    ) {
        this.adminReportService = adminReportService;
        this.userRepository = userRepository;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/users-by-joining-date")
    public ResponseEntity<Page<AdminReportUserRowResponse>> getUsersByJoiningDate(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String search
    ) {
        User currentUser = getCurrentUser();
        validateRequestedAreaAccess(currentUser, sambhagId, districtId, blockId);

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminReportUserRowResponse> response = adminReportService.getUsersByJoiningDate(
                currentUser,
                parseOptionalDate(fromDate),
                parseOptionalDate(toDate),
                sambhagId,
                districtId,
                blockId,
                search,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users-by-retirement-date")
    public ResponseEntity<Page<AdminReportUserRowResponse>> getUsersByRetirementDate(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String search
    ) {
        User currentUser = getCurrentUser();
        validateRequestedAreaAccess(currentUser, sambhagId, districtId, blockId);

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminReportUserRowResponse> response = adminReportService.getUsersByRetirementDate(
                currentUser,
                parseOptionalDate(fromDate),
                parseOptionalDate(toDate),
                sambhagId,
                districtId,
                blockId,
                search,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/no-login-three-months")
    public ResponseEntity<Page<AdminReportUserRowResponse>> getNoLoginThreeMonthsUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String search
    ) {
        User currentUser = getCurrentUser();
        validateRequestedAreaAccess(currentUser, sambhagId, districtId, blockId);

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminReportUserRowResponse> response = adminReportService.getNoLoginThreeMonthsUsers(
                currentUser,
                sambhagId,
                districtId,
                blockId,
                search,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/no-sahyog-two-months")
    public ResponseEntity<Page<AdminReportUserRowResponse>> getNoSahyogTwoMonthsUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String search
    ) {
        User currentUser = getCurrentUser();
        validateRequestedAreaAccess(currentUser, sambhagId, districtId, blockId);

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminReportUserRowResponse> response = adminReportService.getNoSahyogTwoMonthsUsers(
                currentUser,
                sambhagId,
                districtId,
                blockId,
                search,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated user");
        }

        return userRepository.findById(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return LocalDate.parse(value.trim());
    }

    private void validateRequestedAreaAccess(
            User currentUser,
            String sambhagId,
            String districtId,
            String blockId
    ) {
        if (managerScopeService.isAdminOrSuperAdmin(currentUser)) {
            return;
        }

        if (!managerScopeService.isManager(currentUser)) {
            throw new IllegalArgumentException("You are not allowed to view this report");
        }

        if (blockId != null && !blockId.isBlank()) {
            if (!managerScopeService.hasAccessToBlock(currentUser, UUID.fromString(blockId))) {
                throw new IllegalArgumentException("You do not have access to this block");
            }
        }

        if (districtId != null && !districtId.isBlank()) {
            if (!managerScopeService.hasAccessToDistrict(currentUser, UUID.fromString(districtId))) {
                throw new IllegalArgumentException("You do not have access to this district");
            }
        }

        if (sambhagId != null && !sambhagId.isBlank()) {
            if (!managerScopeService.hasAccessToSambhag(currentUser, UUID.fromString(sambhagId))) {
                throw new IllegalArgumentException("You do not have access to this sambhag");
            }
        }
    }
}